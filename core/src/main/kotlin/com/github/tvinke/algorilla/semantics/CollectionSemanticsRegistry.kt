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
 *
 * This is the single source of truth for method classification. All hardcoded method name sets
 * in the codebase should derive from this registry instead of maintaining their own lists.
 */
@Suppress("TooManyFunctions", "LongParameterList")
public class CollectionSemanticsRegistry private constructor(
    private val methodsByLanguage: Map<Language, Map<String, MethodSemantics>>,
    private val heavyweightByLanguage: Map<Language, Set<String>>,
    private val o1ByLanguage: Map<Language, Set<String>>,
    private val streamOpsByLanguage: Map<Language, Set<String>>,
    private val scopeOpsByLanguage: Map<Language, Set<String>>,
    private val trivialByLanguage: Map<Language, Set<String>>,
    private val builderByLanguage: Map<Language, Set<String>>,
    private val getterPrefixesByLanguage: Map<Language, List<String>>,
    private val cheapByLanguage: Map<Language, Set<String>>,
    private val sequentialReadByLanguage: Map<Language, Set<String>>,
    private val reflectionByLanguage: Map<Language, Set<String>>,
    private val copyOnModifyByLanguage: Map<Language, Set<String>>,
    private val regexTypesByLanguage: Map<Language, Set<String>>,
    private val implicitRegexByLanguage: Map<Language, Set<String>>,
    private val mutationByLanguage: Map<Language, Set<String>>,
    private val removalByLanguage: Map<Language, Set<String>>,
    private val bulkLoadPrefixesByLanguage: Map<Language, List<String>>,
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

    /**
     * Returns true if the method is a stream/collection pipeline operation that should be
     * stripped from variable names and not cross-method resolved.
     *
     * A method is a stream op if it appears in the `stream-ops` section OR has a semantic
     * category (any classified method is part of a collection pipeline).
     */
    public fun isStreamOp(
        language: Language,
        methodName: String,
    ): Boolean {
        val resolved = resolveLanguage(language)
        if (streamOpsByLanguage[resolved]?.contains(methodName) == true) return true
        if (scopeOpsByLanguage[resolved]?.contains(methodName) == true) return true
        return methodsByLanguage[resolved]?.containsKey(methodName) == true
    }

    /**
     * Returns the union of all stream ops and classified methods across all languages.
     * Useful when the language is not known (e.g. in extractVariableName).
     */
    public fun allStreamOps(): Set<String> {
        val result = mutableSetOf<String>()
        for (ops in streamOpsByLanguage.values) result.addAll(ops)
        for (ops in scopeOpsByLanguage.values) result.addAll(ops)
        for (methods in methodsByLanguage.values) result.addAll(methods.keys)
        return result
    }

    /**
     * Returns the union of all method names that should not be cross-method resolved.
     * This includes stream ops, scope functions, and all classified methods.
     */
    public fun allUnresolvableNames(): Set<String> = allStreamOps()

    /**
     * Returns true if the method is too cheap to flag as a redundant expensive call.
     */
    public fun isTrivial(
        language: Language,
        methodName: String,
    ): Boolean {
        val resolved = resolveLanguage(language)
        return trivialByLanguage[resolved]?.contains(methodName) == true
    }

    /**
     * Returns the union of all trivial methods across all languages.
     */
    public fun allTrivialMethods(): Set<String> = trivialByLanguage.values.flatten().toSet()

    /**
     * Returns true if the method is a builder-pattern method.
     */
    public fun isBuilder(
        language: Language,
        methodName: String,
    ): Boolean {
        val resolved = resolveLanguage(language)
        return builderByLanguage[resolved]?.contains(methodName) == true
    }

    /**
     * Returns the union of all builder methods across all languages.
     */
    public fun allBuilderMethods(): Set<String> = builderByLanguage.values.flatten().toSet()

    /**
     * Returns true if the method only exists on O(1) types (e.g. containsKey, containsValue).
     */
    public fun isImplicitlyO1(
        language: Language,
        methodName: String,
    ): Boolean {
        val resolved = resolveLanguage(language)
        return methodsByLanguage[resolved]?.get(methodName)?.isImplicitlyO1 == true
    }

    /**
     * Returns the union of all implicitly-O(1) method names across all languages.
     */
    public fun allImplicitlyO1Methods(): Set<String> =
        methodsByLanguage.values
            .flatMap { methods ->
                methods.filter { it.value.isImplicitlyO1 }.map { it.key }
            }.toSet()

    /**
     * Returns getter prefixes for the given language.
     */
    public fun getterPrefixes(language: Language): List<String> {
        val resolved = resolveLanguage(language)
        return getterPrefixesByLanguage[resolved] ?: emptyList()
    }

    /**
     * Returns the union of all getter prefixes across all languages.
     */
    public fun allGetterPrefixes(): List<String> =
        getterPrefixesByLanguage.values
            .flatten()
            .distinct()

    /**
     * Returns true if the method is a known cheap/constant-time operation for the language.
     */
    public fun isCheap(
        language: Language,
        methodName: String,
    ): Boolean {
        val resolved = resolveLanguage(language)
        return cheapByLanguage[resolved]?.contains(methodName) == true
    }

    /**
     * Returns the union of all cheap methods across all languages.
     */
    public fun allCheapMethods(): Set<String> = cheapByLanguage.values.flatten().toSet()

    /**
     * Returns true if the method is a sequential read / stateful iteration method.
     */
    public fun isSequentialRead(
        language: Language,
        methodName: String,
    ): Boolean {
        val resolved = resolveLanguage(language)
        return sequentialReadByLanguage[resolved]?.contains(methodName) == true
    }

    /**
     * Returns the union of all sequential read methods across all languages.
     */
    public fun allSequentialReadMethods(): Set<String> = sequentialReadByLanguage.values.flatten().toSet()

    /**
     * Returns the union of all reflection methods across all languages.
     */
    public fun allReflectionMethods(): Set<String> = reflectionByLanguage.values.flatten().toSet()

    /**
     * Returns the union of all copy-on-modify methods across all languages.
     */
    public fun allCopyOnModifyMethods(): Set<String> = copyOnModifyByLanguage.values.flatten().toSet()

    /**
     * Returns the union of all regex types across all languages.
     */
    public fun allRegexTypes(): Set<String> = regexTypesByLanguage.values.flatten().toSet()

    /**
     * Returns the union of all implicit regex methods across all languages.
     */
    public fun allImplicitRegexMethods(): Set<String> = implicitRegexByLanguage.values.flatten().toSet()

    /**
     * Returns the union of all mutation methods across all languages.
     */
    public fun allMutationMethods(): Set<String> = mutationByLanguage.values.flatten().toSet()

    /**
     * Returns the union of all removal methods across all languages.
     */
    public fun allRemovalMethods(): Set<String> = removalByLanguage.values.flatten().toSet()

    /**
     * Returns the union of all bulk-load prefixes across all languages.
     */
    public fun allBulkLoadPrefixes(): List<String> =
        bulkLoadPrefixesByLanguage.values
            .flatten()
            .distinct()

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
         * Framework-specific YAML overlays that get merged into their parent language.
         * Each framework YAML uses the same section format and adds entries additively.
         */
        private val FRAMEWORK_FILES =
            mapOf(
                Language.JAVASCRIPT to
                    listOf(
                        "semantics/frameworks/react.yml",
                        "semantics/frameworks/vue.yml",
                        "semantics/frameworks/angular.yml",
                        "semantics/frameworks/node.yml",
                        "semantics/frameworks/lodash.yml",
                        "semantics/frameworks/rxjs.yml",
                    ),
                Language.JAVA to
                    listOf(
                        "semantics/frameworks/spring.yml",
                        "semantics/frameworks/guava.yml",
                    ),
                Language.KOTLIN to
                    listOf(
                        "semantics/frameworks/ktor.yml",
                        "semantics/frameworks/coroutines.yml",
                    ),
                Language.GROOVY to
                    listOf(
                        "semantics/frameworks/grails.yml",
                        "semantics/frameworks/spock.yml",
                    ),
            )

        /**
         * Loads the default registry from classpath YAML resources.
         */
        @Suppress("LongMethod")
        public fun loadDefaults(): CollectionSemanticsRegistry {
            val maps = LanguageMaps()

            for ((lang, resource) in LANGUAGE_FILES) {
                val text = loadResource(resource) ?: continue
                maps.merge(lang, parseYaml(text))
            }

            val frameworkCount = mergeFrameworkOverlays(maps)

            logger.info {
                val total = maps.methods.values.sumOf { it.size }
                "Collection semantics registry loaded: $total methods across ${maps.methods.size} languages" +
                    if (frameworkCount > 0) " ($frameworkCount framework overlays)" else ""
            }

            return CollectionSemanticsRegistry(
                maps.methods,
                maps.heavyweight,
                maps.o1,
                maps.streamOps,
                maps.scopeOps,
                maps.trivial,
                maps.builder,
                maps.getterPrefixes,
                maps.cheap,
                maps.sequentialRead,
                maps.reflection,
                maps.copyOnModify,
                maps.regexTypes,
                maps.implicitRegex,
                maps.mutation,
                maps.removal,
                maps.bulkLoadPrefixes,
            )
        }

        private fun mergeFrameworkOverlays(maps: LanguageMaps): Int {
            var count = 0
            for ((lang, resources) in FRAMEWORK_FILES) {
                for (resource in resources) {
                    val text = loadResource(resource) ?: continue
                    maps.merge(lang, parseYaml(text))
                    count++
                }
            }
            return count
        }

        /**
         * Returns a registry merged with user-provided overrides.
         */
        @Suppress("LongMethod")
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
            return CollectionSemanticsRegistry(
                methodsByLanguage = base.methodsByLanguage,
                heavyweightByLanguage = merged,
                o1ByLanguage = base.o1ByLanguage,
                streamOpsByLanguage = base.streamOpsByLanguage,
                scopeOpsByLanguage = base.scopeOpsByLanguage,
                trivialByLanguage = base.trivialByLanguage,
                builderByLanguage = base.builderByLanguage,
                getterPrefixesByLanguage = base.getterPrefixesByLanguage,
                cheapByLanguage = base.cheapByLanguage,
                sequentialReadByLanguage = base.sequentialReadByLanguage,
                reflectionByLanguage = base.reflectionByLanguage,
                copyOnModifyByLanguage = base.copyOnModifyByLanguage,
                regexTypesByLanguage = base.regexTypesByLanguage,
                implicitRegexByLanguage = base.implicitRegexByLanguage,
                mutationByLanguage = base.mutationByLanguage,
                removalByLanguage = base.removalByLanguage,
                bulkLoadPrefixesByLanguage = base.bulkLoadPrefixesByLanguage,
            )
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

@Suppress("LongParameterList")
internal class LanguageMaps(
    val methods: MutableMap<Language, Map<String, MethodSemantics>> = mutableMapOf(),
    val heavyweight: MutableMap<Language, Set<String>> = mutableMapOf(),
    val o1: MutableMap<Language, Set<String>> = mutableMapOf(),
    val streamOps: MutableMap<Language, Set<String>> = mutableMapOf(),
    val scopeOps: MutableMap<Language, Set<String>> = mutableMapOf(),
    val trivial: MutableMap<Language, Set<String>> = mutableMapOf(),
    val builder: MutableMap<Language, Set<String>> = mutableMapOf(),
    val getterPrefixes: MutableMap<Language, List<String>> = mutableMapOf(),
    val cheap: MutableMap<Language, Set<String>> = mutableMapOf(),
    val sequentialRead: MutableMap<Language, Set<String>> = mutableMapOf(),
    val reflection: MutableMap<Language, Set<String>> = mutableMapOf(),
    val copyOnModify: MutableMap<Language, Set<String>> = mutableMapOf(),
    val regexTypes: MutableMap<Language, Set<String>> = mutableMapOf(),
    val implicitRegex: MutableMap<Language, Set<String>> = mutableMapOf(),
    val mutation: MutableMap<Language, Set<String>> = mutableMapOf(),
    val removal: MutableMap<Language, Set<String>> = mutableMapOf(),
    val bulkLoadPrefixes: MutableMap<Language, List<String>> = mutableMapOf(),
) {
    @Suppress("CyclomaticComplexMethod")
    fun merge(
        lang: Language,
        parsed: ParsedYaml,
    ) {
        methods[lang] = (methods[lang] ?: emptyMap()) + parsed.methods
        heavyweight[lang] = (heavyweight[lang] ?: emptySet()) + parsed.heavyweightTypes
        o1[lang] = (o1[lang] ?: emptySet()) + parsed.o1Types
        streamOps[lang] = (streamOps[lang] ?: emptySet()) + parsed.streamOps
        scopeOps[lang] = (scopeOps[lang] ?: emptySet()) + parsed.scopeOps
        trivial[lang] = (trivial[lang] ?: emptySet()) + parsed.trivialMethods
        builder[lang] = (builder[lang] ?: emptySet()) + parsed.builderMethods
        getterPrefixes[lang] = ((getterPrefixes[lang] ?: emptyList()) + parsed.getterPrefixes).distinct()
        cheap[lang] = (cheap[lang] ?: emptySet()) + parsed.cheapMethods
        sequentialRead[lang] = (sequentialRead[lang] ?: emptySet()) + parsed.sequentialReadMethods
        reflection[lang] = (reflection[lang] ?: emptySet()) + parsed.reflectionMethods
        copyOnModify[lang] = (copyOnModify[lang] ?: emptySet()) + parsed.copyOnModifyMethods
        regexTypes[lang] = (regexTypes[lang] ?: emptySet()) + parsed.regexTypes
        implicitRegex[lang] = (implicitRegex[lang] ?: emptySet()) + parsed.implicitRegexMethods
        mutation[lang] = (mutation[lang] ?: emptySet()) + parsed.mutationMethods
        removal[lang] = (removal[lang] ?: emptySet()) + parsed.removalMethods
        bulkLoadPrefixes[lang] = ((bulkLoadPrefixes[lang] ?: emptyList()) + parsed.bulkLoadPrefixes).distinct()
    }
}

internal data class ParsedYaml(
    val methods: Map<String, MethodSemantics>,
    val heavyweightTypes: Set<String>,
    val o1Types: Set<String>,
    val streamOps: Set<String>,
    val scopeOps: Set<String>,
    val trivialMethods: Set<String>,
    val builderMethods: Set<String>,
    val getterPrefixes: List<String>,
    val cheapMethods: Set<String>,
    val sequentialReadMethods: Set<String>,
    val reflectionMethods: Set<String>,
    val copyOnModifyMethods: Set<String>,
    val regexTypes: Set<String>,
    val implicitRegexMethods: Set<String>,
    val mutationMethods: Set<String>,
    val removalMethods: Set<String>,
    val bulkLoadPrefixes: List<String>,
)

/**
 * Lightweight YAML parser for the semantics files. These files have a simple, predictable
 * structure so we avoid pulling in a full YAML library dependency for core.
 */
internal fun parseYaml(text: String): ParsedYaml {
    val sections = splitSections(text)
    val methods = mutableMapOf<String, MethodSemantics>()
    sections["methods"]?.forEach { parseMethodLine(it, methods) }

    return ParsedYaml(
        methods = methods,
        heavyweightTypes = collectListItems(sections["heavyweight-types"]),
        o1Types = collectListItems(sections["o1-types"]),
        streamOps = collectListItems(sections["stream-ops"]),
        scopeOps = collectListItems(sections["scope-ops"]),
        trivialMethods = collectListItems(sections["trivial-methods"]),
        builderMethods = collectListItems(sections["builder-methods"]),
        getterPrefixes = collectListItems(sections["getter-prefixes"]).toList(),
        cheapMethods = collectListItems(sections["cheap-methods"]),
        sequentialReadMethods = collectListItems(sections["sequential-read-methods"]),
        reflectionMethods = collectListItems(sections["reflection-methods"]),
        copyOnModifyMethods = collectListItems(sections["copy-on-modify-methods"]),
        regexTypes = collectListItems(sections["regex-types"]),
        implicitRegexMethods = collectListItems(sections["implicit-regex-methods"]),
        mutationMethods = collectListItems(sections["mutation-methods"]),
        removalMethods = collectListItems(sections["removal-methods"]),
        bulkLoadPrefixes = collectListItems(sections["bulk-load-prefixes"]).toList(),
    )
}

@Suppress("LoopWithTooManyJumpStatements")
private fun splitSections(text: String): Map<String, List<String>> {
    val sections = mutableMapOf<String, MutableList<String>>()
    var currentSection: String? = null
    for (rawLine in text.lines()) {
        val line = rawLine.trimEnd()
        if (line.isBlank() || line.trimStart().startsWith("#")) continue
        if (!line.startsWith(" ") && !line.startsWith("\t") && line.endsWith(":")) {
            currentSection = line.removeSuffix(":").trim()
            continue
        }
        if (currentSection != null) {
            sections.getOrPut(currentSection) { mutableListOf() }.add(line)
        }
    }
    return sections
}

private fun collectListItems(lines: List<String>?): Set<String> =
    lines
        ?.mapNotNull { parseListItem(it) }
        ?.toSet()
        ?: emptySet()

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
    val implicitlyO1 = props["implicitly-o1"]?.lowercase() == "true"

    methods[methodName] =
        MethodSemantics(
            category = category,
            lookupKind = lookupKind,
            sortKind = sortKind,
            accessKind = accessKind,
            complexity = complexity,
            note = note,
            isImplicitlyO1 = implicitlyO1,
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
