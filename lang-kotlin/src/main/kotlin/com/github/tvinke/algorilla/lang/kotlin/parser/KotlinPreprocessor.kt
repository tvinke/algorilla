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

private val paramPattern = Regex("""(\w+)\s*:\s*(\w[\w<>,? ]*)""")

/**
 * Converts Kotlin parameter syntax `name: Type` to Java-style `Type name`.
 * Applied inside parentheses of function signatures.
 */
private fun replaceParameterSyntax(source: String): String =
    source.replace(Regex("""\(([^)]*)\)""")) { matchResult ->
        val inner = matchResult.groupValues[1]
        val converted =
            paramPattern.replace(inner) { m ->
                "${m.groupValues[2]} ${m.groupValues[1]}"
            }
        "($converted)"
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
