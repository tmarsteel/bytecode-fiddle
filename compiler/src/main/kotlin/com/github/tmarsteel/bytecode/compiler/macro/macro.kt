package com.github.tmarsteel.bytecode.compiler.macro;

import com.github.tmarsteel.bytecode.compiler.*
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