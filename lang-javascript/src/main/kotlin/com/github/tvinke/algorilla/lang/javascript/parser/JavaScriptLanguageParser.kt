package com.github.tvinke.algorilla.lang.javascript.parser

import com.github.tvinke.algorilla.engine.LanguageParser
import com.github.tvinke.algorilla.engine.ParserRegistry
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
public class JavaScriptLanguageParser : LanguageParser {
    override val language: Language = Language.JAVASCRIPT

    private val supportedExtensions = setOf(".js", ".mjs", ".cjs", ".ts", ".tsx", ".jsx", ".vue")

    override fun canParse(filePath: String): Boolean = supportedExtensions.any { filePath.endsWith(it) }

    override fun parse(filePath: String): FileRoot {
        val file = File(filePath)
        if (!file.exists()) {
            logger.warn { "File not found: $filePath" }
            return FileRoot(filePath = filePath, language = language, location = SourceLocation(filePath, 1, 1), children = emptyList())
        }

        val (source, lang) = readSource(file)
        val treeSitterResult = parseWithTreeSitter(filePath, source, lang)
        if (treeSitterResult == null) {
            logger.warn { "Tree-sitter failed for $filePath, falling back to regex scanner" }
        }
        val children = treeSitterResult ?: parseWithRegex(filePath, source)
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

    public companion object {
        init {
            ParserRegistry.register(JavaScriptLanguageParser())
        }
    }
}

internal fun tsLanguageFor(lang: Language): TSLanguage =
    when (lang) {
        Language.TYPESCRIPT -> TreeSitterTypescript()
        else -> TreeSitterJavascript()
    }

private val vueScriptPattern = Regex("""<script(\s[^>]*)?>([\s\S]*?)</script>""")
private val vueLangTsPattern = Regex("""lang=["']ts["']""")

/**
 * Reads source content and detects the analysis language in a single pass.
 * For Vue SFCs, extracts the `<script>` block and detects `lang="ts"` from the
 * same read — no second file access needed.
 */
internal fun readSource(file: File): Pair<String, Language> {
    val raw = file.readText()
    if (!file.name.endsWith(".vue")) {
        val lang =
            when {
                file.name.endsWith(".ts") || file.name.endsWith(".tsx") -> Language.TYPESCRIPT
                else -> Language.JAVASCRIPT
            }
        return Pair(raw, lang)
    }
    // Vue SFC: extract <script> block and detect lang="ts" from the tag attributes
    val match = vueScriptPattern.find(raw) ?: return Pair("", Language.JAVASCRIPT)
    val attrs = match.groupValues[1]
    val lang = if (vueLangTsPattern.containsMatchIn(attrs)) Language.TYPESCRIPT else Language.JAVASCRIPT
    return Pair(match.groupValues[2], lang)
}
