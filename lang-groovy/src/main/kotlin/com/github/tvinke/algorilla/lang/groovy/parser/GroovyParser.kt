package com.github.tvinke.algorilla.lang.groovy.parser

import com.github.tvinke.algorilla.engine.LanguageParser
import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.SourceLocation

/**
 * Parses Groovy source files into the unified IR representation.
 */
public class GroovyParser : LanguageParser {
    override val language: Language = Language.GROOVY

    override fun canParse(filePath: String): Boolean = filePath.endsWith(".groovy")

    override fun parse(filePath: String): FileRoot {
        // Placeholder — will be implemented in Fase 5
        return FileRoot(
            filePath = filePath,
            language = Language.GROOVY,
            location = SourceLocation(filePath, 1, 1),
            children = emptyList(),
        )
    }
}
