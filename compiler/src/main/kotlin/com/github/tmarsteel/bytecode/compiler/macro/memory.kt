package com.github.tmarsteel.bytecode.compiler.macro

import com.github.tmarsteel.bytecode.compiler.Location
import com.github.tmarsteel.bytecode.compiler.SyntaxError
import com.github.tmarsteel.bytecode.compiler.isRegisterArgument

/**
 * Allocates a new stackframe on the stack; jumps ot the given target address (parameter 0); On return, writes the
 * return value of the invoked stackframe to #a2.
 * Takes at least one parameter. Parameters 2..$ must be literals. They are interpreted as indexes of the parameters
 * in the current stackframe. Those values are passed on to the new stackframe in the order passed to this macro.
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

        tokens.subList(1, tokens.size).forEach {
            if (isRegisterArgument(it)) {
                throw SyntaxError("All parameters to {${this.javaClass.simpleName} macro except the 0th must be literals", includeLocation)
            }
        }

        val out: MutableList<String> = mutableListOf()

        val myInvocationNr = synchronized(invocationCounter) { invocationCounter++ }
        val returnJumpLabel = ":_invocationReturnLbl$myInvocationNr"
        val targetAddrParam = tokens[0]
        val parameters = tokens.subList(1, tokens.size)

        // make sure the target jump address is loaded into #m8
        out.addAll(assureParameterValueInRegister(targetAddrParam to "#m8"))

        // #m6 will be the address of the current stackframe
        // #m7 will be the address of the new stackframe

        out.addAll("""
        // locate the end of current stackframe
        // current stackframe addr into #a1, #m6
        ldc 65534 #m1
        rcl #m1 #a1
        mov #a1 #m6
        // calculate address of # of parameters in #a1
        ldc 3 #a2
        add
        // recall # of parameters in #a2
        rcl #a1 #a2
        // calculate (end of current stackframe + 1) in #a1
        add
        inc #a1
        // #a1 has now got the address of the new stackframe, write to memory and #m6
        ldc 65534 #m1
        sto #a1 #m1
        mov #a1 #m7
        // write the previousStackframeAddr of the new stackframe
        sto #m6 #a1
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
        inc #a1
        """.split('\n'))

        // write all parameters
        // #m6 is the address of the current stackframe
        // make #m5 hold the address of the first parameter of the new stackframe
        out += "mov #a1 #m5"
        for (currentSFIndex in parameters) {
            // calculate source address
            out += "mov #m6 #a1"
            out += "ldc 4 #a2"
            out += "add"
            out += "ldc $currentSFIndex #a2"
            out += "add"
            // #a1 now holds the source address
            out += "rcl #a1 #a1"
            // #a1 now holds the value of the parameter
            // write that to the address stored in #m5
            out += "sto #a1 #m5"
            out += "inc #m5"
        }

        // do the jump
        out += "vjmp #m8"

        // remember the return address
        out += returnJumpLabel

        // write the return value to #a1 and reset current stackframe
        out.addAll("""
        // address of the stackframe that has just returned into #a1
        ldc 65534 #a1
        rcl #a1 #a1
        // store that address in #m8
        mov #a1 #m8
        // #a1 + 2 points to the return value; store that return value in #m7
        ldc 2 #a2
        add
        rcl #a1 #m7
        // #m8 is a ** to the previous stackframe
        // reset current stackframe addr
        rcl #m8 #m8
        ldc 65534 #a1
        sto #m8 #a1
        // store the return value in #m7 into #a2
        mov #m7 #a2
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

        if (!tokens.isEmpty())
        {
            // assure the return value is loaded into #m8
            out.addAll(assureParameterValueInRegister(tokens[0] to  "#m8"))

            out.addAll("""
            ldc 65534 #m1
            rcl #m1 #a1
            ldc 2 #a2
            add
            // #a1 now holds the address of the return value
            sto #m8 #a1
            dec #a1
            rcl #a1 #a1
            // #a1 now holds the return jump address
            vjmp #a1
            """.split('\n'))
        }
        else
        {
            out += "ldc 65534 #m1"
            out += "rcl #m1 #a1"
            out += "inc #a1"
            out += "rcl #a1 #a1"
            out += "vjmp #a1"
        }

        return out
    }
}

/**
 * Enlargens the parameters space of the current stackframe by X QWORDs (where X is the first parameter to this macro)
 */
val EnlargeCurrentStackframeMacro = object : MacroCommand {
    override fun unroll(tokens: List<String>, includeLocation: Location): List<String> {
        if (tokens.size != 1) {
            throw SyntaxError("The ${this.javaClass.simpleName} macro requires exactly 1 parameter, ${tokens.size} given", includeLocation)
        }

        val out: MutableList<String> = mutableListOf()

        // assure the parameter value is loaded into #m8
        out.addAll(assureParameterValueInRegister(tokens[0] to "#m8"))

        out.addAll("""
        ldc 65534 #m1
        rcl #m1 #a1
        // #a1 now holds the address of the current stackframe
        // #a1 + 3 is the address of the # of parameters / QWORDs in the frame
        ldc 3 #a2
        add
        // store that address in #m7
        mov #a1 #m7
        // recall the number of arguments into #a1
        rcl #a1 #a1
        // increase
        mov #m8 #a2
        add
        // write the number back
        sto #a1 #m7
        """.split('\n'))

        return out
    }
}

/**
 * Writes the value of the first parameter to the Nth QWORD of the current stackframe (where N is the second parameter)
 */
val StoreInStackMacro = object : MacroCommand {
    override fun unroll(tokens: List<String>, includeLocation: Location): List<String> {
        if (tokens.size != 2) {
            throw SyntaxError("The ${this.javaClass.simpleName} macro requires exactly 2 parameters, ${tokens.size} given", includeLocation)
        }

        val out: MutableList<String> = mutableListOf()

        // assure the parameters are loaded into their registers
        out.addAll(assureParameterValueInRegister(tokens[0] to "#m8", tokens[1] to "#a2"))

        out.addAll("""
        ldc 65534 #a1
        rcl #a1 #a1
        // #a1 holds the address of the current stackframe
        add
        // that has added the index in the stackframe to the address
        // add 4 more to the address to correct the offset
        ldc 4 #a2
        add
        // #a1 holds the address of the target stackframe parameter
        sto #m8 #a1
        """.split("\n"))

        return out
    }
}

/**
 * Recalls the value of the Nth QWORD of the current stackframe into the register given as the second parameter (where N
 * is the first parameter)
 */
val RecallFromStackMacro = object : MacroCommand {
    override fun unroll(tokens: List<String>, includeLocation: Location): List<String> {
        if (tokens.size != 2) {
            throw SyntaxError("The ${this.javaClass.simpleName} macro requires exactly 2 parameters, ${tokens.size} given", includeLocation)
        }
        if (!isRegisterArgument(tokens[1])) {
            throw SyntaxError("Parameter 2 given to ${this.javaClass.simpleName} must be a register", includeLocation)
        }

        val out: MutableList<String> = mutableListOf()

        // assure the parameters are loaded into their registers
        out.addAll(assureParameterValueInRegister(tokens[0] to "#a2"))

        out.addAll("""
        ldc 65534 #a1
        rcl #a1 #a1
        // #a1 holds the address of the current stackframe
        add
        // that has added the index in the stackframe to the address
        // add 4 more to the address to correct the offset
        ldc 4 #a2
        add
        // #a1 holds the address of the target stackframe parameter
        rcl #a1 ${tokens[1]}
        """.split("\n"))

        return out
    }
}

val DebugCurrentStackframeMacro = object : MacroCommand {
    override fun unroll(tokens: List<String>, includeLocation: Location): List<String> {
        if (tokens.size != 0) {
            throw SyntaxError("The ${this.javaClass.simpleName} macro takes no parameters", includeLocation)
        }

        return """
        ldc 65534 #m8
        rcl #m8 #m8
        mov #m8 #a1
        ldc 3 #a2
        add
        rcl #a1 #a2
        add
        debug_memory_range #m8 #a1
        """.split('\n')
    }
}