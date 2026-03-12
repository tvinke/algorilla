package com.github.tvinke.algorilla.lang.groovy.parser

import com.github.tvinke.algorilla.engine.LanguageParser
import com.github.tvinke.algorilla.engine.ParserRegistry
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
 * Parses Groovy source files into the unified IR representation.
 * Uses the Java ANTLR grammar as a best-effort parser, since most enterprise Groovy
 * (especially with @CompileStatic) is syntactically close to Java.
 * Files that fail to parse are silently skipped with a warning.
 */
public class GroovyLanguageParser : LanguageParser {
    override val language: Language = Language.GROOVY

    override fun canParse(filePath: String): Boolean = filePath.endsWith(".groovy")

    override fun parse(filePath: String): FileRoot {
        val file = File(filePath)
        if (!file.exists()) {
            logger.warn { "File not found: $filePath" }
            return emptyFileRoot(filePath)
        }
        return try {
            parseWithJavaGrammar(file, filePath)
        } catch (
            @Suppress("TooGenericExceptionCaught") e: Exception,
        ) {
            logger.warn { "Groovy file not parseable with Java grammar: $filePath (${e.message})" }
            emptyFileRoot(filePath)
        }
    }

    private fun parseWithJavaGrammar(
        file: File,
        filePath: String,
    ): FileRoot {
        val source = preprocessGroovySource(file.readText())
        val input = CharStreams.fromString(source, filePath)
        val lexer = JavaLexer(input)
        lexer.removeErrorListeners()
        val tokens = CommonTokenStream(lexer)
        val parser = JavaParser(tokens)
        parser.removeErrorListeners()

        val compilationUnit = parser.compilationUnit()
        val children = GroovyIRVisitor(filePath).visit(compilationUnit)

        return FileRoot(
            filePath = filePath,
            language = Language.GROOVY,
            location = SourceLocation(filePath, 1, 1),
            children = children,
        )
    }

    private fun emptyFileRoot(filePath: String): FileRoot =
        FileRoot(
            filePath = filePath,
            language = Language.GROOVY,
            location = SourceLocation(filePath, 1, 1),
            children = emptyList(),
        )

    public companion object {
        init {
            ParserRegistry.register(GroovyLanguageParser())
        }
    }
}

/**
 * Strips Groovy-specific syntax that would cause the Java parser to fail,
 * while preserving line numbers for accurate source locations.
 */
internal fun preprocessGroovySource(source: String): String =
    source
        .replace("def ", "Object ")
        .replace("@CompileStatic", "")
        .replace("@CompileDynamic", "")
        .replace("@TypeChecked", "")
        .replace("@Slf4j", "")
        .replace("@ToString(", "// @ToString(")
        .replace("@EqualsAndHashCode(", "// @EqualsAndHashCode(")
        .replace("@TupleConstructor(", "// @TupleConstructor(")
