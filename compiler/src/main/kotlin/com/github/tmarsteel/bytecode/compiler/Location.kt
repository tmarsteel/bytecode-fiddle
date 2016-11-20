package com.github.tmarsteel.bytecode.compiler

import java.nio.file.Path

/**
 * Models a location in assembler code
 */
data class Location(val file: Path, val line: Int)