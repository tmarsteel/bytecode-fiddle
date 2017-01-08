package com.github.tmarsteel.bytecode.vm

import com.github.tmarsteel.bytecode.binary.Instruction
import com.github.tmarsteel.bytecode.binary.Instruction.Opcode
import io.kotlintest.specs.FeatureSpec

class CoreTest : FeatureSpec() { init {
    val memory = Memory()
    feature("processor") {
        val subject = Core(memory)
        scenario("load_constant") {
            // constants should be loadable into all registers
            for (register in Register.values()) {
                val constant = (Math.random() * 1000).toLong()
                val instruction = Instruction(Opcode.LOAD_CONSTANT, longArrayOf(register.index.toLong(), constant))

                subject.process(instruction)

                subject[register] shouldEqual constant
            }
        }
    }
}}