package com.github.tmarsteel.bytecode.vm

import com.github.tmarsteel.bytecode.binary.Instruction
import com.github.tmarsteel.bytecode.binary.Instruction.Opcode

/**
 * A virtual processing core
 */
class Core(val sharedMemory: Memory) {
    /** The register values */
    private val R: LongArray = LongArray(Register.values().size)

    fun process(instruction: Instruction) {
        when(instruction.opcode) {
            Opcode.LOAD_CONSTANT         -> R[instruction[0].toInt()] = instruction[1]
            Opcode.MOVE                  -> R[instruction[1].toInt()] = R[instruction[0].toInt()]
            Opcode.STORE                 -> sharedMemory[R[instruction[1].toInt()]] = R[instruction[0].toInt()]
            Opcode.RECALL                -> R[instruction[1].toInt()] = sharedMemory[R[instruction[0].toInt()]]
            Opcode.ADD                   -> R[Register.OPERATOR1.index] += R[Register.OPERATOR2.index]
            Opcode.MUL                   -> R[Register.OPERATOR1.index] *= R[Register.OPERATOR2.index]
            Opcode.INCREMENT             -> R[instruction[0].toInt()]++
            Opcode.DECREMENT             -> R[instruction[0].toInt()]--
            Opcode.EQUALS                -> R[Register.OPERATOR1.index] = if (R[Register.OPERATOR1.index] == R[Register.OPERATOR2.index]) 1L else 0L
            Opcode.GREATER_THAN          -> R[Register.OPERATOR1.index] = if (R[Register.OPERATOR1.index] >  R[Register.OPERATOR2.index]) 1L else 0L
            Opcode.GREATER_THAN_OR_EQUAL -> R[Register.OPERATOR1.index] = if (R[Register.OPERATOR1.index] >= R[Register.OPERATOR2.index]) 1L else 0L
            Opcode.LESS_THAN             -> R[Register.OPERATOR1.index] = if (R[Register.OPERATOR1.index] <  R[Register.OPERATOR2.index]) 1L else 0L
            Opcode.LESS_THAN_OR_EQUAL    -> R[Register.OPERATOR1.index] = if (R[Register.OPERATOR1.index] <= R[Register.OPERATOR2.index]) 1L else 0L
            Opcode.OR                    -> R[Register.OPERATOR1.index] = R[Register.OPERATOR1.index] or  R[Register.OPERATOR2.index]
            Opcode.AND                   -> R[Register.OPERATOR1.index] = R[Register.OPERATOR1.index] and R[Register.OPERATOR2.index]
            Opcode.XOR                   -> R[Register.OPERATOR1.index] = R[Register.OPERATOR1.index] xor R[Register.OPERATOR2.index]
            Opcode.JUMP                  -> {
                R[Register.INSTRUCTION_POINTER.index] = instruction[0]
            }
            Opcode.VARJUMP               -> {
                R[Register.INSTRUCTION_POINTER.index] = R[instruction[0].toInt()]
            }
            Opcode.CONDITIONAL_JUMP      -> {
                if (R[Register.OPERATOR1.index] == 1L) {
                    R[Register.INSTRUCTION_POINTER.index] = instruction[0]
                }
            }
            Opcode.CONDITIONAL_VARJUMP   -> {
                if (R[Register.OPERATOR1.index] == 1L) {
                    R[Register.INSTRUCTION_POINTER.index] = R[instruction[0].toInt()]
                }
            }
            Opcode.TERMINATE             -> throw TerminationException()
        }
    }

    operator fun get(r: Register) = R[r.index]
    operator fun set(r: Register, value: Long) {
        R[r.index] = value
    }

    fun runCodeAt(offset: Long) {
        if (offset >= sharedMemory.size) {
            throw InvalidJumpOffsetException(offset)
        }

        R[Register.INSTRUCTION_POINTER.index] = offset

        while (true) {
            val instrPtrBefore = R[Register.INSTRUCTION_POINTER.index]
            val instruction = readInstructionFromMemoryOffset(
                    sharedMemory,
                    instrPtrBefore
            )
            process(instruction)
            val instrPtrAfter = R[Register.INSTRUCTION_POINTER.index]

            if (instrPtrAfter == instrPtrBefore) {
                // instruction did not modify the IP register => advance
                val newInstrPtr = instrPtrBefore + instruction.qWordSize
                if (newInstrPtr >= sharedMemory.size) {
                    throw VMRuntimeException("Reached end of memory during execution")
                }
                R[Register.INSTRUCTION_POINTER.index] = newInstrPtr
            }
            else if (instrPtrAfter >= sharedMemory.size) {
                throw InvalidJumpOffsetException(R[Register.INSTRUCTION_POINTER.index], instrPtrBefore)
            }
        }
    }
}