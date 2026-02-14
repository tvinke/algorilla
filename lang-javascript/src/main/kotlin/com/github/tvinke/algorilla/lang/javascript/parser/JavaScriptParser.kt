package com.github.tvinke.algorilla.lang.javascript.parser

import com.github.tvinke.algorilla.engine.LanguageParser
import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.SourceLocation

/**
 * Parses JavaScript, TypeScript, and Vue SFC files into the unified IR representation.
 */
public class JavaScriptParser : LanguageParser {
    override val language: Language = Language.JAVASCRIPT

    private val supportedExtensions = setOf(".js", ".mjs", ".cjs", ".ts", ".tsx", ".vue")

    override fun canParse(filePath: String): Boolean = supportedExtensions.any { filePath.endsWith(it) }

    override fun parse(filePath: String): FileRoot {
        // Placeholder — will be implemented in Fase 6
        val lang =
            when {
                filePath.endsWith(".ts") || filePath.endsWith(".tsx") -> Language.TYPESCRIPT
                filePath.endsWith(".vue") -> Language.VUE
                else -> Language.JAVASCRIPT
            }
        return FileRoot(
            filePath = filePath,
            language = lang,
            location = SourceLocation(filePath, 1, 1),
            children = emptyList(),
        )
    }
}
