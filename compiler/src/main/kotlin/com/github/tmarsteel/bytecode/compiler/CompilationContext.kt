package com.github.tmarsteel.bytecode.compiler

/**
 * Models the context in which a file of assembler is compiled
 */
class CompilationContext {
    /** maps labels to their instruction offset */
    val labels: MutableMap<String,Int> = mutableMapOf()

    val collectedInstructions: MutableList<DeferredInstruction> = mutableListOf()
}
