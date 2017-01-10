package com.github.tmarsteel.bytecode.compiler

import com.github.tmarsteel.bytecode.compiler.macro.InvokeMacro
import com.github.tmarsteel.bytecode.compiler.macro.MacroCommand
import com.github.tmarsteel.bytecode.compiler.macro.RecallMemoryRegistersMacro
import com.github.tmarsteel.bytecode.compiler.macro.StoreMemoryRegistersMacro
import java.util.*

/**
 * Models the context in which a file of assembler is compiled
 */
class CompilationContext {
    /** maps label names to more information on them */
    val labels: MutableMap<String,Label> = mutableMapOf()

    val collectedInstructions: MutableList<DeferredInstruction> = mutableListOf()

    val macros: MutableMap<String, MacroCommand> = HashMap(PREDEFINED_MACROS)
}

data class Label(val delcarationLocation: Location, val instructionOffset: Int)

val PREDEFINED_MACROS: Map<String, MacroCommand> = mapOf(
        "_stoRegs" to StoreMemoryRegistersMacro,
        "_rclRegs" to RecallMemoryRegistersMacro,
        "_invoke"  to InvokeMacro
)