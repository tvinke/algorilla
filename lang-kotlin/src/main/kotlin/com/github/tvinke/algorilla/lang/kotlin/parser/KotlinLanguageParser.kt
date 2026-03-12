package com.github.tvinke.algorilla.lang.kotlin.parser

import com.github.tvinke.algorilla.engine.LanguageParser
import com.github.tvinke.algorilla.engine.ParserRegistry
import com.github.tvinke.algorilla.lang.java.parser.JavaLexer
import com.github.tvinke.algorilla.lang.java.parser.JavaParser
import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.SourceLocation
import io.github.oshai.kotlinlogging.KotlinLogging
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.treesitter.TSParser
import org.treesitter.TreeSitterKotlin
import java.io.File

private val logger = KotlinLogging.logger {}

/**
 * Parses Kotlin source files into the unified IR representation.
 * Uses tree-sitter-kotlin for proper Kotlin parsing, falling back to
 * the preprocessor + Java ANTLR grammar when tree-sitter fails.
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
            val children =
                parseWithTreeSitter(filePath, source)
                    ?: parseWithJavaGrammar(filePath, source)
                    ?: run {
                        logger.warn { "All parse strategies failed for $filePath, returning empty result" }
                        emptyList()
                    }
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
    ): List<IRNode>? =
        try {
            val parser = TSParser()
            parser.setLanguage(TreeSitterKotlin())
            val tree = parser.parseString(null, source)
            val root = tree.rootNode
            if (root.isNull || root.hasError()) {
                logger.debug { "Tree-sitter parse had errors, falling back: $filePath" }
                null
            } else {
                KotlinTreeSitterVisitor(filePath, source).visit(root)
            }
        } catch (e: Exception) {
            logger.debug { "Tree-sitter failed, falling back to Java grammar: $filePath (${e.message})" }
            null
        }

    @Suppress("TooGenericExceptionCaught")
    private fun parseWithJavaGrammar(
        filePath: String,
        source: String,
    ): List<IRNode>? =
        try {
            val preprocessed = preprocessKotlinSource(source)
            val input = CharStreams.fromString(preprocessed, filePath)
            val lexer = JavaLexer(input)
            lexer.removeErrorListeners()
            val tokens = CommonTokenStream(lexer)
            val parser = JavaParser(tokens)
            parser.removeErrorListeners()

            val compilationUnit = parser.compilationUnit()
            KotlinIRVisitor(filePath).visit(compilationUnit)
        } catch (e: Exception) {
            logger.debug { "Kotlin file not parseable with Java grammar: $filePath (${e.message})" }
            null
        }

    public companion object {
        init {
            ParserRegistry.register(KotlinLanguageParser())
        }
    }
}
