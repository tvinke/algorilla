package com.github.tvinke.algorilla.lang.javascript.parser

import com.github.tvinke.algorilla.engine.LanguageParser
import com.github.tvinke.algorilla.engine.ParseException
import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.SourceLocation
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File

private val logger = KotlinLogging.logger {}

/**
 * Parses JavaScript, TypeScript, and Vue SFC files into the unified IR representation.
 * Uses a regex-based lightweight approach since JS/TS syntax differs substantially from Java.
 */
public class JavaScriptParser : LanguageParser {
    override val language: Language = Language.JAVASCRIPT

    private val supportedExtensions = setOf(".js", ".mjs", ".cjs", ".ts", ".tsx", ".jsx", ".vue")

    override fun canParse(filePath: String): Boolean = supportedExtensions.any { filePath.endsWith(it) }

    override fun parse(filePath: String): FileRoot {
        val file = File(filePath)
        if (!file.exists()) throw ParseException("File not found: $filePath")

        return try {
            val lang = detectLanguage(filePath)
            val source = extractSource(file)
            val children = JsRegexScanner(filePath).scan(source)
            FileRoot(filePath = filePath, language = lang, location = SourceLocation(filePath, 1, 1), children = children)
        } catch (
            @Suppress("TooGenericExceptionCaught") e: Exception,
        ) {
            logger.debug { "JS/TS file not parseable: $filePath (${e.message})" }
            emptyFileRoot(filePath, detectLanguage(filePath))
        }
    }

    private fun emptyFileRoot(
        filePath: String,
        lang: Language,
    ): FileRoot = FileRoot(filePath = filePath, language = lang, location = SourceLocation(filePath, 1, 1), children = emptyList())
}

internal fun detectLanguage(filePath: String): Language =
    when {
        filePath.endsWith(".ts") || filePath.endsWith(".tsx") -> Language.TYPESCRIPT
        filePath.endsWith(".vue") -> Language.VUE
        else -> Language.JAVASCRIPT
    }

private val vueScriptPattern = Regex("""<script(?:\s+lang="ts")?[^>]*>([\s\S]*?)</script>""")

internal fun extractSource(file: File): String {
    val raw = file.readText()
    if (!file.name.endsWith(".vue")) return raw
    val match = vueScriptPattern.find(raw) ?: return ""
    return match.groupValues[1]
}
