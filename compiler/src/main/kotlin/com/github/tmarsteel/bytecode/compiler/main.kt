package com.github.tmarsteel.bytecode.compiler

import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess

fun main(vararg args: String) {
    if (args.size != 1) {
        println("Specify exactly one input file")
        exitProcess(-1)
    }

    val inputFile = Paths.get(args[0])
    if (Files.notExists(inputFile)) {
        println("Input file $inputFile not found.")
        exitProcess(-1)
    }

    compileFile(inputFile, Paths.get(inputFile.toString() + ".tbc"))
}