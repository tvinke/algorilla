package com.github.tvinke.algorilla.lang.kotlin.parser

/**
 * Strips Kotlin-specific syntax that would cause the Java parser to fail,
 * while preserving line numbers for accurate source locations.
 */
internal fun preprocessKotlinSource(source: String): String =
    source
        .let(::replaceFunKeyword)
        .let(::replaceValVar)
        .let(::replaceParameterSyntax)
        .let(::replaceForInLoops)
        .let(::stripKotlinKeywords)
        .let(::stripKotlinAnnotations)
        .let(::insertSemicolons)

private fun replaceFunKeyword(source: String): String = source.replace("fun ", "void ")

private fun replaceValVar(source: String): String =
    source
        .replace(Regex("""\bval\b"""), "Object")
        .replace(Regex("""\bvar\b"""), "Object")

/** Converts Kotlin `for (x in collection)` to Java-style `for (Object x : collection)`. */
private fun replaceForInLoops(source: String): String =
    source.replace(Regex("""for\s*\(\s*(\w+)\s+in\s+""")) { m ->
        "for (Object ${m.groupValues[1]} : "
    }

private val paramPattern = Regex("""(\w+)\s*:\s*(.+)""")

/**
 * Converts Kotlin parameter syntax `name: Type` to Java-style `Type name`.
 * Applied inside parentheses of function signatures.
 */
private fun replaceParameterSyntax(source: String): String =
    source.replace(Regex("""\(([^)]*)\)""")) { matchResult ->
        val inner = matchResult.groupValues[1]
        val converted =
            splitParameters(inner).joinToString(", ") { param ->
                val trimmed = param.trim()
                val m = paramPattern.matchEntire(trimmed)
                if (m != null) "${m.groupValues[2].trim()} ${m.groupValues[1]}" else trimmed
            }
        "($converted)"
    }

/**
 * Splits a parameter list on commas, respecting nested angle brackets so that
 * commas inside generic types like `Map<String, List<Int>>` are not treated as
 * parameter separators.
 */
private fun splitParameters(input: String): List<String> {
    val parts = mutableListOf<String>()
    var depth = 0
    var start = 0
    for (i in input.indices) {
        when (input[i]) {
            '<' -> depth++
            '>' -> depth--
            ',' ->
                if (depth == 0) {
                    parts.add(input.substring(start, i))
                    start = i + 1
                }
        }
    }
    parts.add(input.substring(start))
    return parts
}

private fun stripKotlinKeywords(source: String): String =
    source
        .replace("companion object", "static class CompanionObject")
        .replace(Regex("""\bdata class\b"""), "class")
        .replace(Regex("""\bsealed class\b"""), "abstract class")
        .replace(Regex("""\bobject\b"""), "class")
        .replace(Regex("""\binternal\b"""), "")
        .replace(Regex("""\boverride\b"""), "")
        .replace(Regex("""\bopen\b"""), "")
        .replace(Regex("""\bsuspend\b"""), "")
        .replace(Regex("""\blaterinit\b"""), "")
        .replace(Regex("""\binline\b"""), "")

private fun stripKotlinAnnotations(source: String): String =
    source
        .replace("@JvmStatic", "")
        .replace("@JvmOverloads", "")
        .replace("@JvmField", "")
        .replace(Regex("""@Suppress\([^)]*\)"""), "")

private val skipSemicolonPattern =
    Regex("""^\s*(//|/\*|\*|import\b|package\b|class\b|abstract class\b|@)""")

/**
 * Appends semicolons to statement-like lines so the Java grammar
 * does not silently drop statements during error recovery.
 * Runs as the last preprocessing step, when the source already
 * looks like Java code.
 */
private fun insertSemicolons(source: String): String =
    source.lines().joinToString("\n") { line ->
        val trimmed = line.trimEnd()
        if (needsSemicolon(trimmed)) "$trimmed;" else line
    }

private fun needsSemicolon(trimmed: String): Boolean =
    trimmed.isNotBlank() &&
        !trimmed.endsWith(";") &&
        !trimmed.endsWith("{") &&
        !trimmed.endsWith("}") &&
        !trimmed.endsWith("(") &&
        !trimmed.endsWith(",") &&
        !skipSemicolonPattern.containsMatchIn(trimmed)
