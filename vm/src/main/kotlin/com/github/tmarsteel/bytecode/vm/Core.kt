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
        var incrementInstructionPointer = true
        when(instruction.opcode) {
            Opcode.LOAD_CONSTANT         -> R[instruction.arg1.toInt()] = instruction.arg2
            Opcode.MOVE                  -> R[instruction.arg2.toInt()] = R[instruction.arg1.toInt()]
            Opcode.STORE                 -> sharedMemory[R[instruction.arg2.toInt()]] = R[instruction.arg1.toInt()]
            Opcode.RECALL                -> R[instruction.arg2.toInt()] = sharedMemory[R[instruction.arg1.toInt()]]
            Opcode.ADD                   -> R[Register.OPERATOR1.index] += R[Register.OPERATOR2.index]
            Opcode.MUL                   -> R[Register.OPERATOR1.index] *= R[Register.OPERATOR2.index]
            Opcode.INCREMENT             -> R[instruction.arg1.toInt()]++
            Opcode.DECREMENT             -> R[instruction.arg1.toInt()]--
            Opcode.EQUALS                -> R[Register.OPERATOR1.index] = if (R[Register.OPERATOR1.index] == R[Register.OPERATOR2.index]) 1L else 0L
            Opcode.GREATER_THAN          -> R[Register.OPERATOR1.index] = if (R[Register.OPERATOR1.index] >  R[Register.OPERATOR2.index]) 1L else 0L
            Opcode.GREATER_THAN_OR_EQUAL -> R[Register.OPERATOR1.index] = if (R[Register.OPERATOR1.index] >= R[Register.OPERATOR2.index]) 1L else 0L
            Opcode.LESS_THAN             -> R[Register.OPERATOR1.index] = if (R[Register.OPERATOR1.index] <  R[Register.OPERATOR2.index]) 1L else 0L
            Opcode.LESS_THAN_OR_EQUAL    -> R[Register.OPERATOR1.index] = if (R[Register.OPERATOR1.index] <= R[Register.OPERATOR2.index]) 1L else 0L
            Opcode.OR                    -> R[Register.OPERATOR1.index] = R[Register.OPERATOR1.index] or  R[Register.OPERATOR2.index]
            Opcode.AND                   -> R[Register.OPERATOR1.index] = R[Register.OPERATOR1.index] and R[Register.OPERATOR2.index]
            Opcode.XOR                   -> R[Register.OPERATOR1.index] = R[Register.OPERATOR1.index] xor R[Register.OPERATOR2.index]
            Opcode.JUMP                  -> {
                R[Register.INSTRUCTION_POINTER.index] = instruction.arg1
                incrementInstructionPointer = false
            }
            Opcode.VARJUMP               -> {
                R[Register.INSTRUCTION_POINTER.index] = R[instruction.arg1.toInt()]
                incrementInstructionPointer = false
            }
            Opcode.CONDITIONAL_JUMP      -> {
                if (R[Register.OPERATOR1.index] == 1L) {
                    R[Register.INSTRUCTION_POINTER.index] = instruction.arg1
                    incrementInstructionPointer = false
                }
            }
            Opcode.CONDITIONAL_VARJUMP   -> {
                if (R[Register.OPERATOR1.index] == 1L) {
                    R[Register.INSTRUCTION_POINTER.index] = R[instruction.arg1.toInt()]
                    incrementInstructionPointer = false
                }
            }
            Opcode.CALL                  -> {
                R[Register.RETURN_INSTRUCTION.index] = R[Register.INSTRUCTION_POINTER.index] + 1
                R[Register.INSTRUCTION_POINTER.index] = instruction.arg1
                incrementInstructionPointer = false
            }
            Opcode.TERMINATE             -> throw TerminationException()
        }

        if (incrementInstructionPointer) {
            R[Register.INSTRUCTION_POINTER.index]++
        }
    }

    operator fun get(r: Register) = R[r.index]
    operator fun set(r: Register, value: Long) {
        R[r.index] = value
    }
}