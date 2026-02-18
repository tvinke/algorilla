package com.github.tvinke.algorilla.lang.kotlin.parser

import com.github.tvinke.algorilla.engine.LanguageParser
import com.github.tvinke.algorilla.engine.ParseException
import com.github.tvinke.algorilla.lang.java.parser.JavaLexer
import com.github.tvinke.algorilla.lang.java.parser.JavaParser
import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.SourceLocation
import io.github.oshai.kotlinlogging.KotlinLogging
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import java.io.File

private val logger = KotlinLogging.logger {}

/**
 * Parses Kotlin source files into the unified IR representation.
 * Uses the Java ANTLR grammar as a best-effort parser, since Kotlin syntax
 * is close enough to Java after preprocessing.
 * Files that fail to parse are silently skipped with a warning.
 */
public class KotlinParser : LanguageParser {
    override val language: Language = Language.KOTLIN

    override fun canParse(filePath: String): Boolean = filePath.endsWith(".kt") || filePath.endsWith(".kts")

    override fun parse(filePath: String): FileRoot {
        val file = File(filePath)
        if (!file.exists()) throw ParseException("File not found: $filePath")

        return try {
            parseWithJavaGrammar(file, filePath)
        } catch (
            @Suppress("TooGenericExceptionCaught") e: Exception,
        ) {
            logger.debug { "Kotlin file not parseable with Java grammar: $filePath (${e.message})" }
            emptyFileRoot(filePath)
        }
    }

    private fun parseWithJavaGrammar(
        file: File,
        filePath: String,
    ): FileRoot {
        val source = preprocessKotlinSource(file.readText())
        val input = CharStreams.fromString(source, filePath)
        val lexer = JavaLexer(input)
        lexer.removeErrorListeners()
        val tokens = CommonTokenStream(lexer)
        val parser = JavaParser(tokens)
        parser.removeErrorListeners()

        val compilationUnit = parser.compilationUnit()
        val children = KotlinIRVisitor(filePath).visit(compilationUnit)

        return FileRoot(
            filePath = filePath,
            language = Language.KOTLIN,
            location = SourceLocation(filePath, 1, 1),
            children = children,
        )
    }

    private fun emptyFileRoot(filePath: String): FileRoot =
        FileRoot(
            filePath = filePath,
            language = Language.KOTLIN,
            location = SourceLocation(filePath, 1, 1),
            children = emptyList(),
        )
}
