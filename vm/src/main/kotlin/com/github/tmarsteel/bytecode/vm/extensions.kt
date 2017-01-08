package com.github.tmarsteel.bytecode.vm

import com.github.tmarsteel.bytecode.binary.Instruction

/**
 * Writes this instruction to the given memory from the given offset on. Will write
 * `this.qWordSize` words to the memory.
 */
fun Instruction.writeTo(memory: Memory, offset: Long) {
    memory[offset] = this.opcode.byteValue.toLong()

    for (i in 1..this.opcode.nArgs) {
        memory[offset + i] = this[i - 1]
    }
}

/**
 * Reads an instruction from the given memory starting at the given offset.
 */
fun readInstructionFromMemoryOffset(memory: Memory, atOffset: Long): Instruction {
    val opcode = memory[atOffset].toByte()
    val opcodeInstance = Instruction.Opcode.byByteValue(opcode)
    val args = LongArray(opcodeInstance.nArgs)
    for (i in 0..args.size - 1) {
        args[i] = memory[atOffset + i + 1]
    }

    return Instruction(opcodeInstance, args)
}