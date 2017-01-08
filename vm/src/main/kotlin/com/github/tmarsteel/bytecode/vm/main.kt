package com.github.tmarsteel.bytecode.vm

import com.github.tmarsteel.bytecode.binary.BytecodeReader
import java.io.File
import java.io.FileInputStream
import kotlin.system.exitProcess

/** Location in memory where the boot code is written to */
val INITIAL_CODE_OFFSET = 0x0L

fun main(vararg args: String) {
    if (args.size != 1) {
        println("Specify exactly one input file!")
        exitProcess(-1)
    }

    val memory = Memory()

    // write code into memory
    FileInputStream(File(args[0])).use { inStream ->
        val reader = BytecodeReader(inStream, false)
        var offset = INITIAL_CODE_OFFSET
        while(reader.hasNext()) {
            val instruction = reader.next()
            instruction.writeTo(memory, offset)
            offset += instruction.qWordSize
        }
    }

    // run the core!
    val core = Core(memory)

    try {
        core.runCodeAt(INITIAL_CODE_OFFSET)
    }
    catch (ex: TerminationException) {
        println("Code terminated")
        println(ex)
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
class TerminationException : VMRuntimeException("Terminated")