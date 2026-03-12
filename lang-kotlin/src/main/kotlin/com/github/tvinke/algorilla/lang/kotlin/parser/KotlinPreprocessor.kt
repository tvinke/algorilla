package com.github.tvinke.algorilla.lang.kotlin.parser

/**
 * Strips Kotlin-specific syntax that would cause the Java parser to fail,
 * while preserving line numbers for accurate source locations.
 */
internal fun preprocessKotlinSource(source: String): String =
    source
        .let(::replaceFunKeyword)
        .let(::replaceTypedDeclarations)
        .let(::replaceValVar)
        .let(::replaceParameterSyntax)
        .let(::stripKotlinKeywords)
        .let(::stripKotlinAnnotations)

private fun replaceFunKeyword(source: String): String = source.replace("fun ", "void ")

/**
 * Converts typed val/var declarations `val name: Type` to `Type name` so the
 * Java parser captures the actual type instead of `Object`. Handles fields,
 * constructor parameters, and destructuring. Must run before [replaceValVar].
 */
private val typedDeclPattern = Regex("""\b(val|var)\s+(\w+)\s*:\s*(\w[\w<>,? ]*)""")

@Suppress("MagicNumber")
private fun replaceTypedDeclarations(source: String): String =
    typedDeclPattern.replace(source) { m ->
        "${m.groupValues[3].trim()} ${m.groupValues[2]}"
    }

private fun replaceValVar(source: String): String =
    source
        .replace(Regex("""\bval\b"""), "Object")
        .replace(Regex("""\bvar\b"""), "Object")

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
