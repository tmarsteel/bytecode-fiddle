package com.github.tmarsteel.bytecode.binary

data class Instruction(val opcode: Opcode, val arg1: Long = 0, val arg2: Long = 0) {
    enum class Opcode(val byteValue: Byte) {
        LOAD_CONSTANT(0),
        MOVE(1),
        ADD(2),
        STORE(3),
        RECALL(4),
        MUL(5),
        JUMP(6),
        CONDITIONAL_JUMP(7),
        EQUALS(8),
        GREATER_THAN(9),
        GREATER_THAN_OR_EQUAL(10),
        OR(11),
        AND(12),
        XOR(13),
        INCREMENT(14),
        TERMINATE(15),
        VARJUMP(16),
        CONDITIONAL_VARJUMP(17),
        DECREMENT(18),
        LESS_THAN(19),
        LESS_THAN_OR_EQUAL(20),
        CALL(21)
    }
}