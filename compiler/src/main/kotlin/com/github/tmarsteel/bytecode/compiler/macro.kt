package com.github.tmarsteel.bytecode.compiler;

import java.nio.file.Paths

interface MacroCommand {
    /**
     * Given the macro parameters `tokens`, returns the list of statements to be compiled with the parameters
     * applied.
     */
    fun unroll(tokens: List<String>, includeLocation: Location): List<String>

    fun locationOf(line: Int): Location = Location(Paths.get("#predefinedMacro"), line)

    fun unrollAndCompile(tokens: List<String>, includeLocation: Location, context: CompilationContext): List<DeferredInstruction> {
        val statements = unroll(tokens, includeLocation)

        println("Unrolled macro ${this.javaClass.simpleName} ($tokens):")
        println(statements.joinToString("\n"))

        return compileLines(
                statements,
                { line ->
                    val macroCodeLocation = locationOf(line)
                    MacroLocation(macroCodeLocation.file, macroCodeLocation.line, includeLocation)
                },
                context
        )
    }
}

/**
 * Persists all the memory registers in an 8-QWORD block in memory. Takes one parameter:
 * - addr: The start address to persist to; a literal is interpreted as a literal memory address; a register is read
 *         and its value is used as the memory address
 */
val StoreMemoryRegistersMacro = object : MacroCommand {
    override fun unroll(tokens: List<String>, includeLocation: Location): List<String> {
        if (tokens.size != 1) {
            throw SyntaxError("The ${this.javaClass.simpleName} macro requires exactly 1 parameter, ${tokens.size} given", includeLocation)
        }

        val targetAddr = tokens[0]
        val instructions: MutableList<String> = mutableListOf()

        // assure the target address is loaded into #a2
        if (isRegisterArgument(targetAddr)) {
            if (targetAddr.toUpperCase() != "#A2") {
                instructions.add("mov $targetAddr #a2")
            }
        } else {
            instructions.add("ldc #a2 $targetAddr")
        }

        for (register in 1..8) {
            instructions.add("sto #m$register #a2")
            instructions.add("inc #a2")
        }

        return instructions
    }
}

/**
 * Fills all the memory registers from an 8-QWORD block in memory. Takes one parameter:
 * - addr: The start address to recall from; a literal is interpreted as a literal memory address; a register is read
 *         and its value is used as the memory address
 */
val RecallMemoryRegistersMacro = object : MacroCommand {
    override fun unroll(tokens: List<String>, includeLocation: Location): List<String> {
        if (tokens.size != 1) {
            throw SyntaxError("The ${this.javaClass.simpleName} macro requires exactly 1 parameter, ${tokens.size} given", includeLocation)
        }

        val targetAddr = tokens[0]
        val instructions: MutableList<String> = mutableListOf()

        // assure the target address is loaded into #a2
        if (isRegisterArgument(targetAddr)) {
            if (targetAddr.toUpperCase() != "#A2") {
                instructions.add("mov $targetAddr #a2")
            }
        } else {
            instructions.add("ldc #a2 $targetAddr")
        }

        for (register in 1..8) {
            instructions.add("rcl #a2 #m$register")
            instructions.add("inc #a2")
        }

        return instructions
    }
}