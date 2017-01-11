package com.github.tmarsteel.bytecode.vm

/**
 * Register identifiers
 */
enum class Register(val index: Int) {
    MEMORY1(0),
    MEMORY2(1),
    MEMORY3(2),
    MEMORY4(3),
    MEMORY5(4),
    MEMORY6(5),
    MEMORY7(6),
    MEMORY8(7),
    OPERATOR1(8),
    OPERATOR2(9),
    INSTRUCTION_POINTER(10)
}