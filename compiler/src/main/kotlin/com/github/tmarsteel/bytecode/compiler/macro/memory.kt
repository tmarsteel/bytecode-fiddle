package com.github.tmarsteel.bytecode.compiler.macro

import com.github.tmarsteel.bytecode.compiler.Location
import com.github.tmarsteel.bytecode.compiler.SyntaxError
import com.github.tmarsteel.bytecode.compiler.isRegisterArgument

/**
 * Allocates a new stackframe on the stack; jumps ot the given target address (parameter 0); On return, writes the
 * return value of the invoked stackframe to #a2.
 * Takes at least one parameter. Parameters 2..$ are packed onto the stackframe.
 *
 * Format of stackframes:
 * offset value
 * 0      previousFrameAddr
 * 1      returnJumpAddr
 * 2      return value
 * 3      number of parameters / size of parameters part
 * 4..X   argument values
 */
val InvokeMacro = object : MacroCommand {
    /* Counter per unrolled invocation command; is used to create unique labels per invocation */
    private var invocationCounter: Long = 0L

    override fun unroll(tokens: List<String>, includeLocation: Location): List<String> {
        if (tokens.size < 1) {
            throw SyntaxError("The ${this.javaClass.simpleName} macro requires at least 1 parameter, ${tokens.size} given", includeLocation)
        }

        val out: MutableList<String> = mutableListOf()

        val myInvocationNr = synchronized(invocationCounter) { invocationCounter++ }
        val returnJumpLabel = ":_invocationReturnLbl$myInvocationNr"
        val targetAddrParam = tokens[0]
        val parameters = tokens.subList(1, tokens.size)

        // locate the end of current stackframe
        out.addAll("""
        // current stackframe addr into #a1 and #m8
        ldc 65534 #m1
        rcl #m1 #a1
        mov #a1 #m8
        // calculate address of # of parameters in #a1, #m1
        ldc 3 #a2
        add
        mov #a2 #m1
        // recall # of parameters in #a1
        rcl #a1 #a1
        // calculate (end of current stackframe + 1) in #a1
        mov #a1 #a2
        mov #m1 #a1
        add
        inc #a1
        // #a1 has now got the address of the new stackframe
        ldc 65534 #m1
        sto #a1 #m1
        // write the current stack frame addr
        sto #m8 #a1
        // write return jump addr address
        inc #a1
        ldc $returnJumpLabel #m1
        sto #m1 #a1
        // initialize return value to 0
        inc #a1
        ldc 0 #m1
        sto #m1 #a1
        // write number of parameters
        inc #a1
        ldc ${parameters.size} #m1
        sto #m1 #a1
        """.split('\n'))

        // write all parameters
        for (parameter in tokens) {
            out += "inc #a1"
            if (isRegisterArgument(parameter)) {
                out += "sto $parameter #a1"
            }
            else
            {
                out += "ldc $parameter #m1"
                out += "sto #m1 #a1"
            }
        }

        // do the jump
        if (isRegisterArgument(tokens[0])) {
            out += "vjmp {$targetAddrParam}"
        }
        else
        {
            out += "jmp {$targetAddrParam}"
        }

        // remember the return address
        out += returnJumpLabel

        // write the return value to #a1 and reset current stackframe
        out.addAll("""
        // address of the stackframe that has just returned into #m1
        ldc 65534 #m1
        rcl #m1 #a1
        // #a1 + 1 is the address of previous stackframe
        // reset current stackframe addr
        inc #a1
        rcl #a1 #m2
        sto #m2 #m1
        // #a1 + 1 is the address of the return value
        inc #a1
        rcl #a1 #2
        """.split('\n'))

        return out
    }
}

/**
 * Takes one optional parameter; if given, writes it to the return value address of the current stackframe.
 * Then jumps to the return address of the current stackframe
 */
val ReturnMacro = object: MacroCommand {
    override fun unroll(tokens: List<String>, includeLocation: Location): List<String> {
        if (tokens.size > 1) {
            throw SyntaxError("The ${this.javaClass.simpleName} macro requires at most 1 parameter, ${tokens.size} given", includeLocation)
        }

        val out: MutableList<String> = mutableListOf()

        if (!tokens.isEmpty()) {
            out += "ldc 65534 #m1"
            out += "rcl #m1 #a1"
            out += "ldc 2 #a2"
            out += "add"
            // #a1 now holds the address of the return value
            if (isRegisterArgument(tokens[0])) {
                out += "sto ${tokens[0]} #a1"
            }
            else
            {
                out += "ldc ${tokens[0]} #m1"
                out += "sto #m1 #a1"
            }
            out += "dec #a1"
            // #a1 now holds the return jump address
            out += "jmp #a1"
        }
        else
        {
            out += "ldc 65534 #m1"
            out += "rcl #m1 #a1"
            out += "inc #a1"
            out += "jmp #a1"
        }

        return out
    }
}

/**
 * Enlargens the parameters space of the current stackframe by X QWORDs (where X is the first parameter to this macro)
 */
val EnlargeCurrentStackFrameMacro = object : MacroCommand {
    override fun unroll(tokens: List<String>, includeLocation: Location): List<String> {
        throw UnsupportedOperationException("not implemented") // TODO
    }
}

/**
 * Writes the value of the first parameter to the Nth QWORD of the current stackframe (where N is the second parameter)
 */
val StoreInStackMacro = object : MacroCommand {
    override fun unroll(tokens: List<String>, includeLocation: Location): List<String> {
        throw UnsupportedOperationException("not implemented") // TODO
    }
}

/**
 * Recalls the value of the Nth QWORD of the current stackframe into the register given as the first parameter (where N
 * is the second parameter)
 */
val RecallFromStackMacro = object : MacroCommand {
    override fun unroll(tokens: List<String>, includeLocation: Location): List<String> {
        throw UnsupportedOperationException("not implemented") // TODO
    }
}