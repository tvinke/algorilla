package com.github.tvinke.algorilla.engine

import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.Language

/**
 * Interface for language-specific parsers. Each language module provides an implementation
 * that converts source files into the unified IR representation.
 */
public interface LanguageParser {
    /** The language this parser handles. */
    public val language: Language

    /**
     * Returns true if this parser can handle the given file path (based on extension).
     */
    public fun canParse(filePath: String): Boolean

    /**
     * Parses a source file into a [FileRoot] IR tree.
     *
     * @throws ParseException if the file cannot be parsed
     */
    public fun parse(filePath: String): FileRoot
}
