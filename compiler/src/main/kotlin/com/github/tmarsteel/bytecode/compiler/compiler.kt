package com.github.tmarsteel.bytecode.compiler

import com.github.tmarsteel.bytecode.binary.BytecodeWriter
import com.github.tmarsteel.bytecode.binary.Instruction
import com.github.tmarsteel.bytecode.vm.Register
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.nio.file.Path

fun compileFile(input: Path, output: Path) {

    val context = CompilationContext()

    // compile init code
    val initcodeResource = context.javaClass.getResource("/com/github/tmarsteel/bytecode/compiler/initcode.asm")
    InputStreamReader(initcodeResource.openStream()).useLines { lines ->
        compileLines(
                lines.toList(),
                { line -> Location(initcodeResource, line) },
                context
        )
    }

    FileOutputStream(output.toFile()).use { outStream ->
        val inputLines = input.toFile().readLines()
        val locationGenerator = { line: Int -> Location(input, line) }

        compileLines(inputLines, locationGenerator, context)

        var writer = BytecodeWriter(outStream)
        context.collectedInstructions.forEach {
            writer.write(it.actual)
        }
    }

    printProgram(context)
}

fun printProgram(context: CompilationContext) {

    var instructionOffset = 0
    var offset = 0
    context.collectedInstructions.forEach { defInstruction ->

        val offsetLabel = context.labels.values.find { it.instructionOffset == instructionOffset }
        if (offsetLabel != null) {
            println("----- :" + offsetLabel.name)
        }

        print(offset.toString().padStart(5, '0'))
        print(" ")
        println(defInstruction.actual)

        instructionOffset++
        offset += defInstruction.actual.qWordSize
    }
}

fun compileLines(lines: List<String>, locationOf: (Int) -> Location, context: CompilationContext = CompilationContext()): List<DeferredInstruction> {

    var lineNumber = 1

    lines.forEach {
        val line = it.trim()
        val location = locationOf(lineNumber)

        if (line.startsWith(':')) {
            // label
            val labelName = line.substring(1)
            if (labelName in context.labels) {
                val existingLabel = context.labels[labelName]!!
                throw SyntaxError("Label $labelName already defined in ${existingLabel.delcarationLocation}; duplicate declaration in $location", location)
            }

            context.labels[labelName] = Label(labelName, location, context.collectedInstructions.size)
        }
        else if (!line.isEmpty() && !line.startsWith("//")) {
            val tokens = line.split(" ")
            if (tokens[0] in OPCODE_MAPPING) {
                val opcode = OPCODE_MAPPING[tokens[0]]!!

                if (tokens.size != opcode.nArgs + 1) {
                    throw SyntaxError("Opcode ${opcode.name} defines {$opcode.nArgs} arguments, {$tokens.size - 1} given", location)
                }

                val args = Array<() -> Long>(opcode.nArgs, { index ->
                    parseOpcodeArgument(tokens[1 + index], context, location)
                })

                context.collectedInstructions.add(DeferredInstruction(opcode, args))
            }
            else if (tokens[0] in context.macros) {
                context.macros[tokens[0]]!!.unrollAndCompile(
                    tokens.subList(1, tokens.size),
                    location,
                    context
                )
            }
            else {
                throw UnknownOpcodeException(tokens[0], location)
            }
        }
        lineNumber++
    }

    return context.collectedInstructions
}

fun parseOpcodeArgument(value: String, context: CompilationContext, location: Location): () -> Long {
    // hexadecimal
    if (value.startsWith("0x")) {
        return {java.lang.Long.parseLong(value.substring(2), 16)}
    }
    else if (value.startsWith("0b")) {
        return {java.lang.Long.parseLong(value.substring(2), 2)}
    }
    else if (value.startsWith('#')) {
        val rName = value.substring(1).toUpperCase()
        if (rName in REGISTER_MAPPING) {
            return {REGISTER_MAPPING[rName]!!.index.toLong()}
        }
        else {
            throw UnknownRegisterException(rName, location)
        }
    }
    else if (value.startsWith(':')) {
        val labelName = value.substring(1)
        return {
            if (labelName in context.labels) {
                val instructionIndex = context.labels[labelName]!!.instructionOffset
                context.collectedInstructions
                    .subList(0, instructionIndex)
                    .map({ it.opCode.qWordSize })
                    .sum()
                    .toLong()
            }
            else {
                throw UnknownLabelException(labelName, location)
            }
        }
    }
    else {
        return {java.lang.Long.parseLong(value, 10)}
    }
}

fun parseLiteralArgument(value: String): Long {
    // hexadecimal
    if (value.startsWith("0x")) {
        return java.lang.Long.parseLong(value.substring(2), 16)
    }
    // binary
    else if (value.startsWith("0b")) {
        return java.lang.Long.parseLong(value.substring(2), 2)
    }
    else {
        return java.lang.Long.parseLong(value, 10)
    }
}

fun isRegisterArgument(arg: String): Boolean {
    return arg.startsWith("#") && arg.substring(1).toUpperCase() in REGISTER_MAPPING
}

class DeferredInstruction(val opCode: Instruction.Opcode, private val generator: () -> Instruction) {

    constructor(opcode: Instruction.Opcode, args: Array<() -> Long> ) : this(opcode, { Instruction(opcode, args.map({ it() }).toLongArray() ) })
    { /* NOOP */ }

    val actual: Instruction by lazy { generator() }
}

val OPCODE_MAPPING = mapOf(
        "ldc"    to Instruction.Opcode.LOAD_CONSTANT,
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
        "term"   to Instruction.Opcode.TERMINATE,
        "debug_core_state"   to Instruction.Opcode.DEBUG_CORE_STATE,
        "debug_memory_range" to Instruction.Opcode.DEBUG_MEMORY_RANGE
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
        "IP" to Register.INSTRUCTION_POINTER
)