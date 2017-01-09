package com.github.tmarsteel.bytecode.compiler.macros

import com.github.tmarsteel.bytecode.compiler.Location
import com.github.tmarsteel.bytecode.compiler.MacroCommand
import com.github.tmarsteel.bytecode.compiler.SyntaxError
import com.github.tmarsteel.bytecode.compiler.isRegisterArgument

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