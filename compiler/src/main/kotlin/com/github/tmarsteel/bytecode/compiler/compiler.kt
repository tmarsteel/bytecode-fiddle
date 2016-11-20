package com.github.tmarsteel.bytecode.compiler

import com.github.tmarsteel.bytecode.binary.BytecodeWriter
import com.github.tmarsteel.bytecode.binary.Instruction
import com.github.tmarsteel.bytecode.vm.Register
import java.io.FileOutputStream
import java.nio.file.Path

fun compileFile(input: Path, output: Path) {

    val labels: MutableMap<String,Int> = mutableMapOf()

    FileOutputStream(output.toFile()).use { outStream ->
        var writer = BytecodeWriter(outStream)

        val inputLines = input.toFile().readLines()

        var lineNumber = 1
        var instructionIndex = 0

        // find labels
        inputLines.forEach {
            val line = it.trim()
            if (line.isEmpty() || line.startsWith("//")) {
                // ignored
            }
            else if (line.startsWith(':')) {
                // label line
                labels[line.substring(1)] = instructionIndex
            }
            else {
                instructionIndex++
            }
        }


        inputLines.forEach {
            val line = it.trim()
            val location = Location(input, lineNumber)

            if (line.isEmpty() || line.startsWith(':') || line.startsWith("//")) {
                // ignored
            }
            else {
                val tokens = line.split(" ")
                if (tokens[0] in OPCODE_MAPPING) {
                    val opcode = OPCODE_MAPPING[tokens[0]]!!

                    if (tokens.size != opcode.nArgs + 1) {
                        throw SyntaxError("Opcode ${opcode.name} defines {$opcode.nArgs} arguments, {$tokens.size - 1} given", location)
                    }

                    val args = LongArray(opcode.nArgs)
                    for (argIndex in 0..args.lastIndex) {
                        args[argIndex] = parseOpcodeArgument(tokens[1 + argIndex], labels, location)
                    }

                    writer.write(Instruction(opcode, args))
                } else {
                    throw UnknownOpcodeException(tokens[0], location)
                }

                instructionIndex++
            }
            lineNumber++
        }
    }
}

fun parseOpcodeArgument(value: String, labels: Map<String,Int>, location: Location): Long {
    // hexadecimal
    if (value.startsWith("0x")) {
        return java.lang.Long.parseLong(value.substring(2), 16)
    }
    else if (value.startsWith("0b")) {
        return java.lang.Long.parseLong(value.substring(2), 2)
    }
    else if (value.startsWith('#')) {
        val rName = value.substring(1).toUpperCase()
        if (rName in REGISTER_MAPPING) {
            return REGISTER_MAPPING[rName]!!.index.toLong()
        }
        else {
            throw UnknownRegisterException(rName, location)
        }
    }
    else if (value.startsWith(':')) {
        val labelName = value.substring(1)
        if (labelName in labels) {
            return labels[labelName]!!.toLong()
        }
        else {
            throw UnknownLabelException(labelName, location)
        }
    }
    else {
        return java.lang.Long.parseLong(value, 10)
    }
}

val OPCODE_MAPPING = mapOf(
        "ldcnst" to Instruction.Opcode.LOAD_CONSTANT,
        "mov"    to Instruction.Opcode.MOVE,
        "add"    to Instruction.Opcode.ADD,
        "mul"    to Instruction.Opcode.MUL,
        "inc"    to Instruction.Opcode.INCREMENT,
        "dec"    to Instruction.Opcode.DECREMENT,
        "sto"    to Instruction.Opcode.STORE,
        "rcl"    to Instruction.Opcode.RECALL,
        "jmp"    to Instruction.Opcode.JUMP,
        "cjmp"   to Instruction.Opcode.CONDITIONAL_JUMP,
        "vjmp"   to Instruction.Opcode.VARJUMP,
        "cvjmp"  to Instruction.Opcode.CONDITIONAL_VARJUMP,
        "eq"     to Instruction.Opcode.EQUALS,
        "gt"     to Instruction.Opcode.GREATER_THAN,
        "gte"    to Instruction.Opcode.GREATER_THAN_OR_EQUAL,
        "lt"     to Instruction.Opcode.LESS_THAN,
        "lte"    to Instruction.Opcode.LESS_THAN_OR_EQUAL,
        "or"     to Instruction.Opcode.OR,
        "and"    to Instruction.Opcode.AND,
        "xor"    to Instruction.Opcode.XOR,
        "term"   to Instruction.Opcode.TERMINATE
)

val REGISTER_MAPPING = mapOf(
        "M1" to Register.MEMORY1,
        "M2" to Register.MEMORY2,
        "M3" to Register.MEMORY3,
        "M4" to Register.MEMORY4,
        "M5" to Register.MEMORY5,
        "M6" to Register.MEMORY6,
        "M7" to Register.MEMORY7,
        "M8" to Register.MEMORY8,
        "A1" to Register.OPERATOR1,
        "A2" to Register.OPERATOR2,
        "R"  to Register.RETURN_INSTRUCTION
)