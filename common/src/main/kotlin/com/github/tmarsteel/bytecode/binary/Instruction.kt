package com.github.tmarsteel.bytecode.binary

data class Instruction(val opcode: Byte, val arg1: Long = 0, val arg2: Long = 0) {
    enum class Opcode {
        LOAD_CONSTANT,
        MOVE
    }
}