package com.github.tmarsteel.bytecode.binary

import java.io.OutputStream

/**
 * Writes bytecode to a binary stream; it is the users responsibility to close the output stream
 */
class BytecodeWriter(val out: OutputStream) {
    fun write(vararg instructions: Instruction) {
        val data: ByteArray = ByteArray(instructions.size * 0x11)
        for (index in 0..instructions.lastIndex) {
            data[index * 0x11 + 0x00] = instructions[index].opcode
            data[index * 0x11 + 0x01] = ( instructions[index].arg1         and 0xFF).toByte()
            data[index * 0x11 + 0x02] = ((instructions[index].arg1 shr  8) and 0xFF).toByte()
            data[index * 0x11 + 0x03] = ((instructions[index].arg1 shr 16) and 0xFF).toByte()
            data[index * 0x11 + 0x04] = ((instructions[index].arg1 shr 24) and 0xFF).toByte()
            data[index * 0x11 + 0x05] = ((instructions[index].arg1 shr 32) and 0xFF).toByte()
            data[index * 0x11 + 0x06] = ((instructions[index].arg1 shr 40) and 0xFF).toByte()
            data[index * 0x11 + 0x07] = ((instructions[index].arg1 shr 48) and 0xFF).toByte()
            data[index * 0x11 + 0x08] = ((instructions[index].arg1 shr 56) and 0xFF).toByte()
            data[index * 0x11 + 0x09] = ( instructions[index].arg2         and 0xFF).toByte()
            data[index * 0x11 + 0x0A] = ((instructions[index].arg2 shr  8) and 0xFF).toByte()
            data[index * 0x11 + 0x0B] = ((instructions[index].arg2 shr 16) and 0xFF).toByte()
            data[index * 0x11 + 0x0C] = ((instructions[index].arg2 shr 24) and 0xFF).toByte()
            data[index * 0x11 + 0x0D] = ((instructions[index].arg2 shr 32) and 0xFF).toByte()
            data[index * 0x11 + 0x0E] = ((instructions[index].arg2 shr 40) and 0xFF).toByte()
            data[index * 0x11 + 0x0F] = ((instructions[index].arg2 shr 48) and 0xFF).toByte()
            data[index * 0x11 + 0x10] = ((instructions[index].arg2 shr 56) and 0xFF).toByte()
        }

        out.write(data)
    }
}
