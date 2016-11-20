package com.github.tmarsteel.bytecode.binary

import java.io.OutputStream

/**
 * Writes bytecode to a binary stream; it is the users responsibility to close the output stream
 */
class BytecodeWriter(val out: OutputStream) {

    /**
     * Writes the given instruction to the underlying output stream. Returns the number of bytes written.
     */
    fun write(vararg instructions: Instruction): Int {
        var nBytes = 0
        for (instruction in instructions) {
            out.write(instruction.opcode.byteValue.toInt())
            nBytes++

            for (argIndex in 0..instruction.opcode.nArgs - 1) {
                val arg = instruction[argIndex]

                out.write(((arg shr 56) and 0xFF).toInt())
                out.write(((arg shr 48) and 0xFF).toInt())
                out.write(((arg shr 40) and 0xFF).toInt())
                out.write(((arg shr 32) and 0xFF).toInt())
                out.write(((arg shr 24) and 0xFF).toInt())
                out.write(((arg shr 16) and 0xFF).toInt())
                out.write(((arg shr  8) and 0xFF).toInt())
                out.write(( arg         and 0xFF).toInt())
                nBytes += 8
            }
        }

        return nBytes
    }
}
