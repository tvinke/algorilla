package com.github.tvinke.algorilla.lang.kotlin.parser

import com.github.tvinke.algorilla.engine.LanguageParser
import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.SourceLocation

/**
 * Parses Kotlin source files into the unified IR representation.
 */
public class KotlinParser : LanguageParser {
    override val language: Language = Language.KOTLIN

    override fun canParse(filePath: String): Boolean = filePath.endsWith(".kt") || filePath.endsWith(".kts")

    override fun parse(filePath: String): FileRoot {
        // Placeholder — will be implemented in Fase 7
        return FileRoot(
            filePath = filePath,
            language = Language.KOTLIN,
            location = SourceLocation(filePath, 1, 1),
            children = emptyList(),
        )
    }
}
