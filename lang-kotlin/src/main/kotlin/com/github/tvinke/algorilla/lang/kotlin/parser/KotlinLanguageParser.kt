package com.github.tvinke.algorilla.lang.kotlin.parser

import com.github.tvinke.algorilla.engine.LanguageParser
import com.github.tvinke.algorilla.engine.ParserRegistry
import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.SourceLocation
import io.github.oshai.kotlinlogging.KotlinLogging
import org.treesitter.TSParser
import org.treesitter.TreeSitterKotlin
import java.io.File

private val logger = KotlinLogging.logger {}

/**
 * Parses Kotlin source files into the unified IR representation.
 * Uses tree-sitter-kotlin for proper Kotlin parsing.
 */
public class KotlinLanguageParser : LanguageParser {
    override val language: Language = Language.KOTLIN

    override fun canParse(filePath: String): Boolean = filePath.endsWith(".kt") || filePath.endsWith(".kts")

    override fun parse(filePath: String): FileRoot {
        val file = File(filePath)
        if (!file.exists()) {
            logger.warn { "File not found: $filePath" }
            return emptyFileRoot(filePath)
        }
        return try {
            val source = file.readText()
            val children = parseWithTreeSitter(filePath, source)
            FileRoot(
                filePath = filePath,
                language = Language.KOTLIN,
                location = SourceLocation(filePath, 1, 1),
                children = children,
            )
        } catch (
            @Suppress("TooGenericExceptionCaught") e: Exception,
        ) {
            logger.warn { "Failed to parse $filePath: ${e.message}" }
            emptyFileRoot(filePath)
        }
    }

    private fun emptyFileRoot(filePath: String) =
        FileRoot(
            filePath = filePath,
            language = language,
            location = SourceLocation(filePath, 1, 1),
            children = emptyList(),
        )

    @Suppress("TooGenericExceptionCaught")
    private fun parseWithTreeSitter(
        filePath: String,
        source: String,
    ): List<com.github.tvinke.algorilla.model.IRNode> {
        try {
            val parser = TSParser()
            parser.setLanguage(TreeSitterKotlin())
            val tree = parser.parseString(null, source)
            val root = tree.rootNode
            if (root.isNull || root.hasError()) {
                logger.warn { "Tree-sitter parse had errors for $filePath, returning empty result" }
                return emptyList()
            }
            return KotlinTreeSitterVisitor(filePath, source).visit(root)
        } catch (e: Exception) {
            logger.warn { "Tree-sitter failed for $filePath: ${e.message}" }
            return emptyList()
        }
    }

    public companion object {
        init {
            ParserRegistry.register(KotlinLanguageParser())
        }
    }
}
