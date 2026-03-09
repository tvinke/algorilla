package com.github.tvinke.algorilla.semantics

import com.github.tvinke.algorilla.model.AccessKind
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LookupKind
import com.github.tvinke.algorilla.model.SortKind
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Data-driven registry that maps method names to their collection semantics per language.
 * Loaded from YAML resource files at startup, with optional user overrides from `.algorilla.yml`.
 */
public class CollectionSemanticsRegistry private constructor(
    private val methodsByLanguage: Map<Language, Map<String, MethodSemantics>>,
    private val heavyweightByLanguage: Map<Language, Set<String>>,
    private val o1ByLanguage: Map<Language, Set<String>>,
) {
    /**
     * Classifies a method name for the given language.
     * Returns null if the method has no known semantics.
     */
    public fun classify(
        language: Language,
        methodName: String,
    ): MethodSemantics? {
        val resolved = resolveLanguage(language)
        return methodsByLanguage[resolved]?.get(methodName)
    }

    /**
     * Returns true if the given type name is a known heavyweight type for the language.
     */
    public fun isHeavyweight(
        language: Language,
        typeName: String,
    ): Boolean {
        val resolved = resolveLanguage(language)
        return heavyweightByLanguage[resolved]?.any { typeName.contains(it) } == true
    }

    /**
     * Returns the set of heavyweight type names for the given language.
     */
    public fun heavyweightTypes(language: Language): Set<String> {
        val resolved = resolveLanguage(language)
        return heavyweightByLanguage[resolved] ?: emptySet()
    }

    /**
     * Returns the merged set of all heavyweight types across all languages.
     */
    public fun allHeavyweightTypes(): Set<String> = heavyweightByLanguage.values.flatten().toSet()

    /**
     * Returns true if the given type name indicates an O(1) lookup type.
     */
    public fun isO1Type(typeName: String): Boolean = o1ByLanguage.values.any { types -> types.any { typeName.contains(it) } }

    /**
     * Returns true if the given type name indicates an O(1) lookup type for the language.
     */
    public fun isO1Type(
        language: Language,
        typeName: String,
    ): Boolean {
        val resolved = resolveLanguage(language)
        return o1ByLanguage[resolved]?.any { typeName.contains(it) } == true
    }

    private fun resolveLanguage(language: Language): Language =
        when (language) {
            Language.TYPESCRIPT, Language.VUE -> Language.JAVASCRIPT
            else -> language
        }

    public companion object {
        private val LANGUAGE_FILES =
            mapOf(
                Language.JAVA to "semantics/java.yml",
                Language.GROOVY to "semantics/groovy.yml",
                Language.JAVASCRIPT to "semantics/javascript.yml",
                Language.KOTLIN to "semantics/kotlin.yml",
            )

        /**
         * Loads the default registry from classpath YAML resources.
         */
        public fun loadDefaults(): CollectionSemanticsRegistry {
            val methods = mutableMapOf<Language, Map<String, MethodSemantics>>()
            val heavyweight = mutableMapOf<Language, Set<String>>()
            val o1 = mutableMapOf<Language, Set<String>>()

            for ((lang, resource) in LANGUAGE_FILES) {
                val text = loadResource(resource) ?: continue
                val parsed = parseYaml(text)
                methods[lang] = parsed.methods
                heavyweight[lang] = parsed.heavyweightTypes
                o1[lang] = parsed.o1Types
            }

            logger.info {
                val total = methods.values.sumOf { it.size }
                "Collection semantics registry loaded: $total methods across ${methods.size} languages"
            }

            return CollectionSemanticsRegistry(methods, heavyweight, o1)
        }

        /**
         * Returns a registry merged with user-provided overrides.
         */
        public fun withOverrides(
            base: CollectionSemanticsRegistry,
            userHeavyweightTypes: Set<String>,
        ): CollectionSemanticsRegistry {
            if (userHeavyweightTypes.isEmpty()) return base
            val merged = base.heavyweightByLanguage.toMutableMap()
            for (lang in merged.keys) {
                merged[lang] = merged[lang]!! + userHeavyweightTypes
            }
            // Also add to all languages that don't have an entry yet
            for (lang in Language.entries) {
                if (lang !in merged) {
                    merged[lang] = userHeavyweightTypes
                }
            }
            return CollectionSemanticsRegistry(base.methodsByLanguage, merged, base.o1ByLanguage)
        }

        private fun loadResource(path: String): String? {
            val stream = CollectionSemanticsRegistry::class.java.classLoader.getResourceAsStream(path)
            if (stream == null) {
                logger.warn { "Semantics resource not found: $path" }
                return null
            }
            return stream.bufferedReader().readText()
        }
    }
}

internal data class ParsedYaml(
    val methods: Map<String, MethodSemantics>,
    val heavyweightTypes: Set<String>,
    val o1Types: Set<String>,
)

/**
 * Lightweight YAML parser for the semantics files. These files have a simple, predictable
 * structure so we avoid pulling in a full YAML library dependency for core.
 */
internal fun parseYaml(text: String): ParsedYaml {
    val methods = mutableMapOf<String, MethodSemantics>()
    val heavyweightTypes = mutableSetOf<String>()
    val o1Types = mutableSetOf<String>()

    var currentSection: String? = null
    @Suppress("LoopWithTooManyJumpStatements")
    for (rawLine in text.lines()) {
        val line = rawLine.trimEnd()
        if (line.isBlank() || line.trimStart().startsWith("#")) continue

        // Top-level section detection
        if (!line.startsWith(" ") && !line.startsWith("\t") && line.endsWith(":")) {
            currentSection = line.removeSuffix(":").trim()
            continue
        }

        when (currentSection) {
            "methods" -> parseMethodLine(line, methods)
            "heavyweight-types" -> parseListItem(line)?.let { heavyweightTypes.add(it) }
            "o1-types" -> parseListItem(line)?.let { o1Types.add(it) }
        }
    }

    return ParsedYaml(methods, heavyweightTypes, o1Types)
}

private fun parseMethodLine(
    line: String,
    methods: MutableMap<String, MethodSemantics>,
) {
    // Format: "  methodName: { semantics: category, kind: KIND, ... }"
    // or "  methodName: { semantics: category }"
    val colonIdx = line.indexOf(':')
    if (colonIdx < 0) return
    val methodName = line.substring(0, colonIdx).trim()
    if (methodName.startsWith("-") || methodName.startsWith("#")) return

    val value = line.substring(colonIdx + 1).trim()
    val props = parseBraceProps(value)
    val semanticsStr = props["semantics"] ?: return

    val category = parseCategory(semanticsStr) ?: return
    val lookupKind = props["kind"]?.let { parseLookupKind(it) }
    val sortKind = props["kind"]?.let { parseSortKind(it) }
    val accessKind = props["kind"]?.let { parseAccessKind(it) }
    val complexity = props["complexity"]
    val note = props["note"]

    methods[methodName] =
        MethodSemantics(
            category = category,
            lookupKind = lookupKind,
            sortKind = sortKind,
            accessKind = accessKind,
            complexity = complexity,
            note = note,
        )
}

private fun parseBraceProps(value: String): Map<String, String> {
    val content = value.removePrefix("{").removeSuffix("}").trim()
    if (content.isEmpty()) return emptyMap()
    val props = mutableMapOf<String, String>()
    for (part in content.split(",")) {
        val kv = part.trim().split(":", limit = 2)
        if (kv.size == 2) {
            props[kv[0].trim()] = kv[1].trim().removeSurrounding("\"")
        }
    }
    return props
}

private fun parseListItem(line: String): String? {
    val trimmed = line.trim()
    if (!trimmed.startsWith("- ")) return null
    return trimmed.removePrefix("- ").trim()
}

private fun parseCategory(value: String): SemanticCategory? =
    when (value.lowercase().replace("-", "_")) {
        "iteration" -> SemanticCategory.ITERATION
        "lookup" -> SemanticCategory.LOOKUP
        "sort" -> SemanticCategory.SORT
        "access" -> SemanticCategory.ACCESS
        "quadratic" -> SemanticCategory.QUADRATIC
        "blocking" -> SemanticCategory.BLOCKING
        "serialization" -> SemanticCategory.SERIALIZATION
        "repository_call" -> SemanticCategory.REPOSITORY_CALL
        "copy_on_modify" -> SemanticCategory.COPY_ON_MODIFY
        else -> null
    }

private fun parseLookupKind(value: String): LookupKind? =
    try {
        LookupKind.valueOf(value)
    } catch (_: IllegalArgumentException) {
        null
    }

private fun parseSortKind(value: String): SortKind? =
    try {
        SortKind.valueOf(value)
    } catch (_: IllegalArgumentException) {
        null
    }

private fun parseAccessKind(value: String): AccessKind? =
    try {
        AccessKind.valueOf(value)
    } catch (_: IllegalArgumentException) {
        null
    }
