@file:Suppress("MatchingDeclarationName") // Primary export is the parseYaml function, not the result type

package com.github.tvinke.algorilla.semantics

import com.github.tvinke.algorilla.model.AccessKind
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LookupKind
import com.github.tvinke.algorilla.model.SortKind

internal data class ParsedYaml(
    val methods: Map<String, MethodSemantics>,
    val heavyweightTypes: Set<String>,
    val collectionTypes: Set<String>,
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
    val regexRecompilationMethods: Set<String>,
    val mutationMethods: Set<String>,
    val removalMethods: Set<String>,
    val bulkLoadPrefixes: List<String>,
    val fullScanMethods: Set<String>,
    val ioMethods: Set<String>,
    val o1FactoryMethods: Set<String>,
    val memoizationMethods: Set<String>,
    val treeTraversalAccessors: Set<String>,
    val visitedSetNames: Set<String>,
    val purePrefixes: List<String>,
    val sideEffectPrefixes: List<String>,
    val sideEffectTargets: List<String>,
    val stringIndicators: Set<String>,
    val stringNameSuffixes: Set<String>,
    val stringExactNames: Set<String>,
    val monadicTypes: Set<String>,
    val monadicVarNames: Set<String>,
    val bulkAlternatives: Map<String, String> = emptyMap(),
    val extras: Map<String, Set<String>> = emptyMap(),
)

/**
 * Lightweight YAML parser for the semantics files. These files have a simple, predictable
 * structure so we avoid pulling in a full YAML library dependency for core.
 */
@Suppress("LongMethod") // Straightforward field-to-section mapping, splitting would reduce readability
internal fun parseYaml(text: String): ParsedYaml {
    val sections = splitSections(text)
    val methods = mutableMapOf<String, MethodSemantics>()
    sections["methods"]?.forEach { parseMethodLine(it, methods) }

    // Collect all section keys that aren't explicitly parsed into extras
    val knownSections =
        setOf(
            "methods",
            "heavyweight-types",
            "collection-types",
            "o1-types",
            "stream-ops",
            "scope-ops",
            "trivial-methods",
            "builder-methods",
            "getter-prefixes",
            "cheap-methods",
            "sequential-read-methods",
            "reflection-methods",
            "copy-on-modify-methods",
            "regex-types",
            "regex-recompilation-methods",
            "mutation-methods",
            "removal-methods",
            "bulk-load-prefixes",
            "full-scan-methods",
            "io-methods",
            "o1-factory-methods",
            "memoization-methods",
            "tree-traversal-accessors",
            "visited-set-names",
            "pure-prefixes",
            "side-effect-prefixes",
            "side-effect-targets",
            "string-indicators",
            "string-name-suffixes",
            "string-exact-names",
            "monadic-types",
            "monadic-variable-names",
            "bulk-alternatives",
            "language",
        )
    val extras = mutableMapOf<String, Set<String>>()
    for ((key, lines) in sections) {
        if (key !in knownSections) {
            extras[key] = collectListItems(lines)
        }
    }

    return ParsedYaml(
        methods = methods,
        heavyweightTypes = collectListItems(sections["heavyweight-types"]),
        collectionTypes = collectListItems(sections["collection-types"]),
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
        regexRecompilationMethods = collectListItems(sections["regex-recompilation-methods"]),
        mutationMethods = collectListItems(sections["mutation-methods"]),
        removalMethods = collectListItems(sections["removal-methods"]),
        bulkLoadPrefixes = collectListItems(sections["bulk-load-prefixes"]).toList(),
        fullScanMethods = collectListItems(sections["full-scan-methods"]),
        ioMethods = collectListItems(sections["io-methods"]),
        o1FactoryMethods = collectListItems(sections["o1-factory-methods"]),
        memoizationMethods = collectListItems(sections["memoization-methods"]),
        treeTraversalAccessors = collectListItems(sections["tree-traversal-accessors"]),
        visitedSetNames = collectListItems(sections["visited-set-names"]),
        purePrefixes = collectListItems(sections["pure-prefixes"]).toList(),
        sideEffectPrefixes = collectListItems(sections["side-effect-prefixes"]).toList(),
        sideEffectTargets = collectListItems(sections["side-effect-targets"]).toList(),
        stringIndicators = collectListItems(sections["string-indicators"]),
        stringNameSuffixes = collectListItems(sections["string-name-suffixes"]),
        stringExactNames = collectListItems(sections["string-exact-names"]),
        monadicTypes = collectListItems(sections["monadic-types"]),
        monadicVarNames = collectListItems(sections["monadic-variable-names"]),
        bulkAlternatives = collectMapItems(sections["bulk-alternatives"]),
        extras = extras,
    )
}

/**
 * Extracts the top-level `language:` value from a framework YAML.
 * Returns the corresponding [Language] enum value, or null if not found / unrecognised.
 */
internal fun extractLanguageFromYaml(text: String): Language? {
    val pattern = Regex("""^language:\s*(\w+)\s*$""", RegexOption.MULTILINE)
    val name = pattern.find(text)?.groupValues?.get(1) ?: return null
    return Language.fromName(name)
}

@Suppress("LoopWithTooManyJumpStatements")
internal fun splitSections(text: String): Map<String, List<String>> {
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

@Suppress("LoopWithTooManyJumpStatements")
internal fun collectMapItems(lines: List<String>?): Map<String, String> {
    if (lines == null) return emptyMap()
    val result = mutableMapOf<String, String>()
    for (line in lines) {
        val trimmed = line.trim()
        if (trimmed.startsWith("#") || trimmed.startsWith("-")) continue
        val colonIdx = trimmed.indexOf(':')
        if (colonIdx < 0) continue
        val key = trimmed.substring(0, colonIdx).trim()
        val value = trimmed.substring(colonIdx + 1).trim()
        if (key.isNotEmpty() && value.isNotEmpty()) {
            result[key] = value
        }
    }
    return result
}

internal fun collectListItems(lines: List<String>?): Set<String> =
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
    val value = trimmed.removePrefix("- ").trim()
    // Strip YAML-style quotes (single or double)
    if (value.length >= 2 && value.first() == '"' && value.last() == '"') return value.substring(1, value.length - 1)
    if (value.length >= 2 && value.first() == '\'' && value.last() == '\'') return value.substring(1, value.length - 1)
    return value
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
