package com.github.tvinke.algorilla.model

/**
 * Represents a specific location in a source file, identified by file path, line number, and column.
 */
public data class SourceLocation(
    val file: String,
    val line: Int,
    val column: Int,
) {
    /**
     * Formats the location as `file:line:column` for display in console output.
     */
    public fun format(): String = "$file:$line:$column"
}
