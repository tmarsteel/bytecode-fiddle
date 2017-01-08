package com.github.tmarsteel.bytecode.compiler

import java.nio.file.Path

/**
 * Models a location in assembler code
 */
open class Location(val file: Path, val line: Int) {
    override fun toString(): String {
        return "$file on line $line"
    }
}

class MacroLocation(macroFile: Path, macroFileLine: Int, val includeLocation: Location) : Location(macroFile, macroFileLine) {
    override fun toString(): String {
        return "$file on line $line (included from $includeLocation)"
    }
}