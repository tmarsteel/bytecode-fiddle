package com.github.tmarsteel.bytecode.compiler

open class SyntaxError(val msg: String, val location: Location, cause: Throwable? = null) : Exception("$msg in " + location, cause)
class UnknownOpcodeException(val name: String, location: Location) : SyntaxError("Unknown opcode $name", location)
class UnknownRegisterException(val name: String, location: Location) : SyntaxError("Unknown register $name", location)
class UnknownLabelException(val name: String, location: Location) : SyntaxError("Unknown label $name", location)