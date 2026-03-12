package com.github.tvinke.algorilla.lang.template.parser

import com.github.tvinke.algorilla.engine.LanguageParser
import com.github.tvinke.algorilla.engine.ParserRegistry
import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.SourceLocation
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File

private val logger = KotlinLogging.logger {}

/**
 * Parses <LANGUAGE> source files into the unified IR representation.
 *
 * TODO: rename this class to match your language (e.g. PythonParser, RubyParser).
 * TODO: update the package from `lang.template` to `lang.<yourlanguage>`.
 *
 * See KotlinLanguageParser and JavaLanguageParser for working examples.
 */
public class TemplateParser : LanguageParser {

    // TODO: replace with your Language enum entry (you need to add it to Language.kt first).
    override val language: Language = TODO("Language.YOUR_LANG")

    // TODO: return true for all file extensions your language uses.
    // e.g. filePath.endsWith(".py")
    override fun canParse(filePath: String): Boolean = TODO("check file extension")

    /**
     * Parses a single source file into a [FileRoot] IR tree.
     *
     * Contract: this method must NEVER throw. If anything goes wrong (file missing,
     * parse error, tree-sitter crash), log a warning and return an empty FileRoot.
     * The engine relies on this — a thrown exception would abort the entire analysis run.
     */
    override fun parse(filePath: String): FileRoot {
        val file = File(filePath)
        if (!file.exists()) {
            logger.warn { "File not found: $filePath" }
            return emptyFileRoot(filePath)
        }
        return try {
            val source = file.readText()

            // TODO: parse the source with tree-sitter. Roughly:
            //   val parser = TSParser()
            //   parser.setLanguage(TreeSitterYourLang())
            //   val tree = parser.parseString(null, source)
            //   val root = tree.rootNode
            //   if (root.isNull || root.hasError()) {
            //       logger.warn { "Tree-sitter parse had errors for $filePath" }
            //       return emptyFileRoot(filePath)
            //   }
            //   val children = YourTreeSitterVisitor(filePath, source).visit(root)
            val children: List<IRNode> = TODO("implement tree-sitter parsing")

            FileRoot(
                filePath = filePath,
                language = language,
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

    /**
     * Returns an empty FileRoot for cases where parsing fails or the file is missing.
     * Always prefer this over throwing — the engine must keep going.
     */
    private fun emptyFileRoot(filePath: String) =
        FileRoot(
            filePath = filePath,
            language = language,
            location = SourceLocation(filePath, 1, 1),
            children = emptyList(),
        )

    /**
     * Self-registration via companion object init block. When the CLI or Gradle plugin
     * touches `TemplateParser.Companion`, the class loads and this block runs, registering
     * the parser with [ParserRegistry].
     *
     * TODO: uncomment the init block once your parser is ready.
     *
     * IMPORTANT: you MUST also add `YourParser.Companion` to TWO places:
     *   1. AlgorillaCommand.kt  → registerAllParsers() function
     *   2. AlgorillaTask.kt     → analyze() function
     * If you forget this step, your parser compiles and tests pass, but it will
     * silently never load at runtime — no error, no warning, just no files parsed.
     * This is the #1 gotcha when adding a new language.
     */
    public companion object {
        // init {
        //     ParserRegistry.register(TemplateParser())
        // }
    }
}
