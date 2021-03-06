package com.github.tmarsteel.bytecode.binary

open class Instruction constructor(val opcode: Opcode, private val args: LongArray) {

    init {
        if (args.size != opcode.nArgs) {
            throw IllegalArgumentException("Opcode ${opcode.name} is defined for ${opcode.nArgs} arguments, {$args.size} given.")
        }
    }

    /**
     * The number of long words (QWORD) this instruction occupies.
     */
    val qWordSize: Int = opcode.qWordSize

    operator fun get(argIndex: Int): Long {
        return args[argIndex]
    }

    override fun toString(): String {
        return opcode.name + " " + args.joinToString(" ")
    }

    enum class Opcode(val byteValue: Byte, val nArgs: Int) {
        LOAD_CONSTANT(0, 2),
        MOVE(1, 2),
        ADD(2, 0),
        STORE(3, 2),
        RECALL(4, 2),
        MUL(5, 0),
        JUMP(6, 1),
        CONDITIONAL_JUMP(7, 1),
        EQUALS(8, 0),
        GREATER_THAN(9, 0),
        GREATER_THAN_OR_EQUAL(10, 0),
        OR(11, 0),
        AND(12, 0),
        XOR(13, 0),
        INCREMENT(14, 1),
        TERMINATE(15, 0),
        VARJUMP(16, 1),
        CONDITIONAL_VARJUMP(17, 1),
        DECREMENT(18, 1),
        LESS_THAN(19, 0),
        LESS_THAN_OR_EQUAL(20, 0),
        DEBUG_CORE_STATE(21, 0),
        DEBUG_MEMORY_RANGE(22, 0);

        /** The number of long words (QWORD) an instruction with this opcode needs */
        val qWordSize: Int = nArgs + 1

        companion object {
            /**
             * Returns the opcode with the given value.
             * @throws UnknownOpcodeException If the given byteValue does not map to an opcode.
             */
            fun byByteValue(byteValue: Byte): Opcode {
                return Opcode.values().find { it.byteValue == byteValue } ?: throw UnknownOpcodeException(byteValue)
            }
        }
    }
}

class UnknownOpcodeException(val opcode: Byte) : RuntimeException("Unknown opcode $opcode")