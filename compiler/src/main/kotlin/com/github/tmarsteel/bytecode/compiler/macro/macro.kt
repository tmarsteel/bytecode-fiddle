package com.github.tmarsteel.bytecode.compiler.macro;

import com.github.tmarsteel.bytecode.compiler.*
import java.nio.file.Paths

interface MacroCommand {
    /**
     * Given the macro parameters `tokens`, returns the list of statements to be compiled with the parameters
     * applied.
     */
    fun unroll(tokens: List<String>, includeLocation: Location): List<String>

    fun locationOf(line: Int): Location = Location(Paths.get("macro ${this.javaClass.simpleName}"), line)

    fun unrollAndCompile(tokens: List<String>, includeLocation: Location, context: CompilationContext): List<DeferredInstruction> {
        val statements = unroll(tokens, includeLocation)

        println("Unrolled macro ${this.javaClass.simpleName} ($tokens):")
        println(statements.joinToString("\n"))

        return compileLines(
                statements,
                { line ->
                    MacroLocation(locationOf(line), includeLocation)
                },
                context
        )
    }
}

/**
 * Returns some assembly code that assures the the value of the given parameters is stored in the given registers
 * The pair keys are the macro parameters, the entry values the target register, including #m8
 * Can be invoked like so:
 *     assureParameterValueInRegister(tokens[0] to "m7", tokens[1] to "m8")
 */
fun assureParameterValueInRegister(vararg assurances: Pair<String, String>): List<String> {

    val out: MutableList<String> = mutableListOf()

    for ((parameter, targetRegister) in assurances) {
        if (isRegisterArgument(parameter)) {
            if (parameter.toLowerCase() != targetRegister.toLowerCase()) {
                out += "mov $parameter $targetRegister"
            }
        }
        else
        {
            out += "ldc $parameter $targetRegister"
        }
    }

    return out
}