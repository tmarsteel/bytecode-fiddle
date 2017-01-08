package com.github.tmarsteel.bytecode.compiler

import java.util.*

/**
 * Models the context in which a file of assembler is compiled
 */
class CompilationContext {
    /** maps labels to their instruction offset */
    val labels: MutableMap<String,Int> = mutableMapOf()

    val collectedInstructions: MutableList<DeferredInstruction> = mutableListOf()

    val macros: MutableMap<String,MacroCommand> = HashMap(PREDEFINED_MACROS)
}

val PREDEFINED_MACROS: Map<String,MacroCommand> = mapOf(
        "_stoRegs" to StoreMemoryRegistersMacro,
        "_rclRegs" to RecallMemoryRegistersMacro
)