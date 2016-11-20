package com.github.tmarsteel.bytecode.vm

import com.github.tmarsteel.bytecode.binary.BytecodeReader
import java.io.File
import java.io.FileInputStream
import kotlin.system.exitProcess

fun main(vararg args: String) {
    if (args.size != 1) {
        println("Specify exactly one input file!")
        exitProcess(-1)
    }

    val core = Core(Memory())
    val reader = BytecodeReader(FileInputStream(File(args[0])))
    val instructions = reader.asSequence().toList()

    while (true) {
        val instruction = try {
            instructions[core[Register.INSTRUCTION_POINTER].toInt()]
        }
        catch (ex: ArrayIndexOutOfBoundsException) {
            throw InvalidJumpOffset(core[Register.INSTRUCTION_POINTER])
        }

        // println(instruction.opcode.name + " " + instruction.arg1 + " " + instruction.arg2)
        try {
            core.process(instruction)
        }
        catch (ex: TerminationException) {
            break
        }

        // printCoreState(core)
        // println()

        if (core[Register.INSTRUCTION_POINTER] == instructions.size.toLong()) {
            // end of code reached
            break
        }
    }

    printCoreState(core)
}

fun printCoreState(core: Core) {
    Register.values().forEach { register ->
        println(register.name + ": " + core[register].toString())
    }
    println("-----")
}

open class VMRuntimeException(msg: String) : RuntimeException(msg)
class InvalidJumpOffset(val offset: Long) : VMRuntimeException("Invalid jump offset $offset; cannot jump")
class TerminationException : VMRuntimeException("Terminated")