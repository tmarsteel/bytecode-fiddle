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

    val memory = Memory(65535)

    // write code into memory starting at 0x0000
    var offset = 0x0L
    FileInputStream(File(args[0])).use { inStream ->
        val reader = BytecodeReader(inStream, false)
        println("Reading code...")
        while(reader.hasNext()) {
            val instruction = reader.next()
            instruction.writeTo(memory, offset)
            offset += instruction.qWordSize
            println(instruction)
        }
        println("-----------------------------")
    }

    // setup the core
    val core = Core(memory)

    // write boot code meta
    core[Register.MEMORY1] = offset

    try {
        core.runCodeAt(0x0L)
    }
    catch (ex: TerminationException) {
        println("Code terminated")
        println(ex)
    }
}

fun printCoreState(core: Core) {
    Register.values().forEach { register ->
        println(register.name + ": " + core[register].toString())
    }
    println("-----")
}

open class VMRuntimeException(msg: String) : RuntimeException(msg)
class TerminationException : VMRuntimeException("Terminated")