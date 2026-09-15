package com.github.tvinke.algorilla.engine

import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.SourceLocation
import io.github.oshai.kotlinlogging.KLogger
import java.io.File

/**
 * Builds an empty [FileRoot] for a file that couldn't be found or parsed.
 */
private fun emptyFileRoot(
    filePath: String,
    language: Language,
): FileRoot =
    FileRoot(
        filePath = filePath,
        language = language,
        location = SourceLocation(filePath, 1, 1),
        children = emptyList(),
    )

/**
 * Guards a language-specific [parseBody] with the file-existence check and catch-all
 * error handling every [LanguageParser] implementation needs: a missing file or a parse
 * failure both fall back to an empty [FileRoot], logged through [logger]. [onParseFailure]
 * lets a caller customize the warning text (e.g. Groovy explaining it re-uses the Java
 * grammar); it defaults to the generic message used by the straightforward ANTLR/tree-sitter
 * parsers.
 */
public fun parseFileOrEmpty(
    filePath: String,
    language: Language,
    logger: KLogger,
    onParseFailure: (Exception) -> Any? = { "Failed to parse $filePath: ${it.message}" },
    parseBody: (File) -> List<IRNode>,
): FileRoot {
    val file = File(filePath)
    if (!file.exists()) {
        logger.warn { "File not found: $filePath" }
        return emptyFileRoot(filePath, language)
    }
    return try {
        FileRoot(
            filePath = filePath,
            language = language,
            location = SourceLocation(filePath, 1, 1),
            children = parseBody(file),
        )
    } catch (
        @Suppress("TooGenericExceptionCaught") e: Exception,
    ) {
        logger.warn { onParseFailure(e) }
        emptyFileRoot(filePath, language)
    }
}
