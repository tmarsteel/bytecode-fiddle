package com.github.tmarsteel.bytecode.compiler.macro

/**
 * Allocates a new stackframe on the stack. Takes one parameter:
 * - previousAddr: The address of the previous
 */
val AllocateStackframeMacro = object : MacroCommand {
    fun unroll(tokens: List<String>): List<String> {

    }
}