package com.github.tvinke.algorilla.lang.javascript.parser

import com.github.tvinke.algorilla.engine.LanguageParser
import com.github.tvinke.algorilla.engine.ParseException
import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.SourceLocation
import io.github.oshai.kotlinlogging.KotlinLogging
import org.treesitter.TSLanguage
import org.treesitter.TSParser
import org.treesitter.TreeSitterJavascript
import org.treesitter.TreeSitterTypescript
import java.io.File

private val logger = KotlinLogging.logger {}

/**
 * Parses JavaScript, TypeScript, and Vue SFC files into the unified IR representation.
 * Uses tree-sitter for proper parse tree analysis, with regex-based fallback.
 */
public class JavaScriptParser : LanguageParser {
    override val language: Language = Language.JAVASCRIPT

    private val supportedExtensions = setOf(".js", ".mjs", ".cjs", ".ts", ".tsx", ".jsx", ".vue")

    override fun canParse(filePath: String): Boolean = supportedExtensions.any { filePath.endsWith(it) }

    override fun parse(filePath: String): FileRoot {
        val file = File(filePath)
        if (!file.exists()) throw ParseException("File not found: $filePath")

        val lang = detectLanguage(filePath)
        val source = extractSource(file)
        val children =
            parseWithTreeSitter(filePath, source, lang)
                ?: parseWithRegex(filePath, source)
        return FileRoot(filePath = filePath, language = lang, location = SourceLocation(filePath, 1, 1), children = children)
    }

    @Suppress("TooGenericExceptionCaught")
    private fun parseWithTreeSitter(
        filePath: String,
        source: String,
        lang: Language,
    ): List<IRNode>? =
        try {
            val tsLang = tsLanguageFor(lang)
            val parser = TSParser()
            parser.setLanguage(tsLang)
            val tree = parser.parseString(null, source)
            val root = tree.rootNode
            if (root.isNull || root.hasError()) {
                logger.debug { "Tree-sitter parse had errors, falling back: $filePath" }
                null
            } else {
                JsTreeSitterVisitor(filePath, source).visit(root)
            }
        } catch (e: Exception) {
            logger.debug { "Tree-sitter failed, falling back to regex: $filePath (${e.message})" }
            null
        }

    @Suppress("TooGenericExceptionCaught")
    private fun parseWithRegex(
        filePath: String,
        source: String,
    ): List<IRNode> =
        try {
            JsRegexScanner(filePath).scan(source)
        } catch (e: Exception) {
            logger.debug { "JS/TS file not parseable: $filePath (${e.message})" }
            emptyList()
        }
}

internal fun tsLanguageFor(lang: Language): TSLanguage =
    when (lang) {
        Language.TYPESCRIPT -> TreeSitterTypescript()
        else -> TreeSitterJavascript()
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
