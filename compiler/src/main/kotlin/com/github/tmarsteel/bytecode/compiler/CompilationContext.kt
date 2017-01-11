package com.github.tmarsteel.bytecode.compiler

import com.github.tmarsteel.bytecode.compiler.macro.*
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

data class Label(val name: String, val delcarationLocation: Location, val instructionOffset: Int)

val PREDEFINED_MACROS: Map<String, MacroCommand> = mapOf(
        "_stoRegs"  to StoreMemoryRegistersMacro,
        "_rclRegs"  to RecallMemoryRegistersMacro,
        "_sf_enlg"  to EnlargeCurrentStackframeMacro,
        "_sf_sto"   to StoreInStackMacro,
        "_sf_rcl"   to RecallFromStackMacro,
        "_invoke"   to InvokeMacro,
        "_return"   to ReturnMacro,
        "_sf_debug" to DebugCurrentStackframeMacro
)