package com.github.tmarsteel.bytecode.compiler

import java.net.URL
import java.nio.file.Path

/**
 * Models a location in assembler code
 */
open class Location(val file: String, val line: Int) {

    constructor(file: Path, line: Int): this(file.toString(), line)

    constructor(file: URL, line: Int): this(file.toExternalForm(), line)

    override fun toString(): String {
        return "$file on line $line"
    }
}

class MacroLocation(val macroFileLocation: Location, val includeLocation: Location) : Location(macroFileLocation.file, macroFileLocation.line) {
    override fun toString(): String {
        return "$macroFileLocation (included from $includeLocation)"
    }
}