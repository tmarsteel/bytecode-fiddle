package com.github.tmarsteel.bytecode.binary

import java.io.IOException
import java.io.InputStream

/**
 * Reads instructions from a binary file
 */
class BytecodeReader(private val input: InputStream, val closeOnEOF: Boolean = true) : Iterator<Instruction> {

    private var next: Instruction? = null
    private var inputClosed: Boolean = false
    private var offsetCounter: Long = 0

    init {
        next = readNext()
    }

    override fun next(): Instruction {
        val current = next ?: throw RuntimeException("End of stream")
        next = readNext()

        return current
    }

    override fun hasNext(): Boolean = next != null

    private fun readNext(): Instruction? {
        val opcode = try {
            readByte()
        }
        catch (ex: UnexpectedEOFException) {
            if (closeOnEOF) close()
            return null
        }

        for (opcodeInst in Instruction.Opcode.values()) {
            if (opcodeInst.byteValue == opcode) {
                var args = LongArray(opcodeInst.nArgs)
                for (argIndex in 0..args.lastIndex) {
                    args[argIndex] = readArg()
                }
                return Instruction(opcodeInst, args)
            }
        }

        throw UnknownOpcodeException(opcode, offsetCounter)
    }

    private fun readArg(): Long {
        return  (readByte().toLong() shl 56) or
                (readByte().toLong() shl 48) or
                (readByte().toLong() shl 40) or
                (readByte().toLong() shl 32) or
                (readByte().toLong() shl 24) or
                (readByte().toLong() shl 16) or
                (readByte().toLong() shl  8) or
                 readByte().toLong()
    }

    private fun readByte(): Byte {
        val byte = input.read()
        if (byte == -1) {
            throw UnexpectedEOFException(offsetCounter)
        }

        offsetCounter++

        return byte.toByte()
    }

    fun close() {
        if (!inputClosed) {
            input.close()
            inputClosed = true
        }
    }

    companion object {
        open class BinaryParseException(message: String, cause: Throwable? = null) : IOException(message, cause)
        class UnknownOpcodeException(val opcode: Byte, val offset: Long) : BinaryParseException("Unknown opcode $opcode at offset $offset")
        class UnexpectedEOFException(val offset: Long) : BinaryParseException("Unexpected EOF at offset $offset")
    }
}