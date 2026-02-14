package com.github.tvinke.algorilla.lang.java.parser

import com.github.tvinke.algorilla.engine.LanguageParser
import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.SourceLocation

/**
 * Parses Java source files into the unified IR representation.
 * Uses ANTLR with the Java grammar for robust parsing.
 */
public class JavaParser : LanguageParser {
    override val language: Language = Language.JAVA

    override fun canParse(filePath: String): Boolean = filePath.endsWith(".java")

    override fun parse(filePath: String): FileRoot {
        // Placeholder — will be implemented in Fase 1
        return FileRoot(
            filePath = filePath,
            language = Language.JAVA,
            location = SourceLocation(filePath, 1, 1),
            children = emptyList(),
        )
    }
}
