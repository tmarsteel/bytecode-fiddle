package com.github.tmarsteel.bytecode.vm

/**
 * Models variable-sized memory
 */
class Memory(initSize: Int = 0xFFFF)
{
    private var data: LongArray = LongArray(initSize);

    var size: Int
        get() = data.size
        set(value) {
            data = data.copyOf(value)
        }

    operator fun get(address: Long): Long {
        if (address < 0 || address > size - 1) {
            throw MemoryBoundsExceededException(address, size)
        }

        return data[address.toInt()]
    }

    operator fun set(address: Long, value: Long) {
        if (address < 0 || address > size - 1) {
            throw MemoryBoundsExceededException(address, size)
        }

        data[address.toInt()] = value
    }

    companion object {
        open class MemoryException(message: String) : ArrayIndexOutOfBoundsException(message)
        class MemoryBoundsExceededException(val targetAddress: Long, val memorySize: Int) : MemoryException("Address $targetAddress exceeds the memory space of $memorySize cells.")
    }
}