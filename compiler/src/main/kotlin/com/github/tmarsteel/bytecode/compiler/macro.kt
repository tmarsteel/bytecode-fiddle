package com.github.tmarsteel.bytecode.compiler;

interface MacroCommand {
    /**
     * Given the macro parameters `tokens`, returns the list of statements to be compiled with the parameters
     * applied.
     */
    fun unroll(tokens: List<String>): List<String>

    fun locationOf(line: Int): Location

    fun unrollAndCompile(tokens: List<String>, includeLocation: Location, context: CompilationContext): List<DeferredInstruction> {
        val statements = unroll(tokens)
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