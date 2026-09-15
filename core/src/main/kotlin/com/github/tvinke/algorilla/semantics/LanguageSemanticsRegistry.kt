package com.github.tvinke.algorilla.semantics

import com.github.tvinke.algorilla.model.AccessKind
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LookupKind
import com.github.tvinke.algorilla.model.SortKind
import com.github.tvinke.algorilla.util.containsAnyAtWordBoundary
import com.github.tvinke.algorilla.util.containsAtWordBoundary
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Data-driven registry that maps method names to their semantics per language.
 * Loaded from YAML resource files at startup, with optional user overrides from `.algorilla.yml`.
 *
 * This is the single source of truth for method classification. All hardcoded method name sets
 * in the codebase should derive from this registry instead of maintaining their own lists.
 */
@Suppress("TooManyFunctions", "LargeClass") // Registry of query methods — each is a one-liner delegating to LanguageMaps
public class LanguageSemanticsRegistry private constructor(
    private val maps: LanguageMaps,
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
        return maps.methods[resolved]?.get(methodName)
    }

    /**
     * Returns true if the given type name is a known heavyweight type for the language.
     */
    public fun isHeavyweight(
        language: Language,
        typeName: String,
    ): Boolean {
        val resolved = resolveLanguage(language)
        // ignoreCase = false: type names are case-sensitive identifiers (PascalCase by
        // convention), and some callers pass a receiver variable name here too - keeping
        // case-sensitive avoids matching a lowercase variable like "pattern" against a real
        // "Pattern" type name.
        return containsAnyAtWordBoundary(typeName, maps.heavyweight[resolved] ?: emptySet(), ignoreCase = false)
    }

    /**
     * Returns the set of heavyweight type names for the given language.
     */
    public fun heavyweightTypes(language: Language): Set<String> {
        val resolved = resolveLanguage(language)
        return maps.heavyweight[resolved] ?: emptySet()
    }

    /**
     * Returns true if the given type name is a known collection type for the language.
     * Checks both `collection-types` and `o1-types` (the union of all collection-like types).
     */
    public fun isCollectionType(
        language: Language,
        typeName: String,
    ): Boolean {
        val resolved = resolveLanguage(language)
        return matchesTypeName(typeName, maps.collectionTypes[resolved], extraSection(language, "non-collection-type-names")) ||
            matchesTypeName(typeName, maps.o1[resolved], extraSection(language, "non-o1-type-names"))
    }

    /**
     * Returns true if the given type name indicates an O(1) lookup type, in any language.
     * Iterates `maps.o1.keys` (the languages actually loaded) rather than [Language.entries]:
     * this is on the hot path (once per lookup-shaped call site during parsing), and
     * [Language.entries] includes `TYPESCRIPT`, which [resolveLanguage] aliases to
     * `JAVASCRIPT` - iterating the full enum would re-scan the same candidate/exclusion
     * sets for that alias a second time.
     */
    public fun isO1Type(typeName: String): Boolean = maps.o1.keys.any { isO1Type(it, typeName) }

    /**
     * Returns true if the given type name indicates an O(1) lookup type for the language.
     */
    public fun isO1Type(
        language: Language,
        typeName: String,
    ): Boolean {
        val resolved = resolveLanguage(language)
        return matchesTypeName(typeName, maps.o1[resolved], extraSection(language, "non-o1-type-names"))
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
        if (maps.streamOps[resolved]?.contains(methodName) == true) return true
        if (maps.scopeOps[resolved]?.contains(methodName) == true) return true
        return maps.methods[resolved]?.containsKey(methodName) == true
    }

    /**
     * Returns stream ops and classified methods for the given language.
     */
    public fun streamOps(language: Language): Set<String> {
        val resolved = resolveLanguage(language)
        val result = mutableSetOf<String>()
        maps.streamOps[resolved]?.let { result.addAll(it) }
        maps.scopeOps[resolved]?.let { result.addAll(it) }
        maps.methods[resolved]?.keys?.let { result.addAll(it) }
        return result
    }

    /**
     * Returns method names that should not be cross-method resolved for the given language.
     */
    public fun unresolvableNames(language: Language): Set<String> = streamOps(language)

    /**
     * Returns true if the method is too cheap to flag as a redundant expensive call.
     */
    public fun isTrivial(
        language: Language,
        methodName: String,
    ): Boolean {
        val resolved = resolveLanguage(language)
        return maps.trivial[resolved]?.contains(methodName) == true
    }

    /**
     * Returns trivial methods for the given language.
     */
    public fun trivialMethods(language: Language): Set<String> {
        val resolved = resolveLanguage(language)
        return maps.trivial[resolved] ?: emptySet()
    }

    /**
     * Returns true if the method is a builder-pattern method.
     */
    public fun isBuilder(
        language: Language,
        methodName: String,
    ): Boolean {
        val resolved = resolveLanguage(language)
        return maps.builder[resolved]?.contains(methodName) == true
    }

    /**
     * Returns builder methods for the given language.
     */
    public fun builderMethods(language: Language): Set<String> {
        val resolved = resolveLanguage(language)
        return maps.builder[resolved] ?: emptySet()
    }

    /**
     * Returns true if the method only exists on O(1) types (e.g. containsKey, containsValue).
     */
    public fun isImplicitlyO1(
        language: Language,
        methodName: String,
    ): Boolean {
        val resolved = resolveLanguage(language)
        return maps.methods[resolved]?.get(methodName)?.isImplicitlyO1 == true
    }

    /**
     * Returns implicitly-O(1) method names for the given language.
     */
    public fun implicitlyO1Methods(language: Language): Set<String> {
        val resolved = resolveLanguage(language)
        return maps.methods[resolved]
            ?.filter { it.value.isImplicitlyO1 }
            ?.keys ?: emptySet()
    }

    /**
     * Returns getter prefixes for the given language.
     */
    public fun getterPrefixes(language: Language): List<String> {
        val resolved = resolveLanguage(language)
        return maps.getterPrefixes[resolved] ?: emptyList()
    }

    /**
     * Returns true if the method is a known cheap/constant-time operation for the language.
     */
    public fun isCheap(
        language: Language,
        methodName: String,
    ): Boolean {
        val resolved = resolveLanguage(language)
        return maps.cheap[resolved]?.contains(methodName) == true
    }

    /**
     * Returns cheap methods for the given language.
     */
    public fun cheapMethods(language: Language): Set<String> {
        val resolved = resolveLanguage(language)
        return maps.cheap[resolved] ?: emptySet()
    }

    /**
     * Returns true if the method is a sequential read / stateful iteration method.
     */
    public fun isSequentialRead(
        language: Language,
        methodName: String,
    ): Boolean {
        val resolved = resolveLanguage(language)
        return maps.sequentialRead[resolved]?.contains(methodName) == true
    }

    /**
     * Returns sequential read methods for the given language.
     */
    public fun sequentialReadMethods(language: Language): Set<String> {
        val resolved = resolveLanguage(language)
        return maps.sequentialRead[resolved] ?: emptySet()
    }

    /**
     * Returns reflection methods for the given language.
     */
    public fun reflectionMethods(language: Language): Set<String> {
        val resolved = resolveLanguage(language)
        return maps.reflection[resolved] ?: emptySet()
    }

    /**
     * Returns the copy-on-modify methods for a specific language.
     */
    public fun copyOnModifyMethodsFor(language: Language): Set<String> {
        val resolved = resolveLanguage(language)
        return maps.copyOnModify[resolved] ?: emptySet()
    }

    /**
     * Returns regex types for the given language.
     */
    public fun regexTypes(language: Language): Set<String> {
        val resolved = resolveLanguage(language)
        return maps.regexTypes[resolved] ?: emptySet()
    }

    /**
     * Returns regex recompilation methods for a specific language.
     */
    public fun regexRecompilationMethods(language: Language): Set<String> {
        val resolved = resolveLanguage(language)
        return maps.regexRecompilation[resolved] ?: emptySet()
    }

    /**
     * Returns mutation methods for the given language.
     */
    public fun mutationMethods(language: Language): Set<String> {
        val resolved = resolveLanguage(language)
        return maps.mutation[resolved] ?: emptySet()
    }

    /**
     * Returns removal methods for a specific language.
     */
    public fun removalMethods(language: Language): Set<String> {
        val resolved = resolveLanguage(language)
        return maps.removal[resolved] ?: emptySet()
    }

    public fun bulkLoadPrefixes(language: Language): List<String> {
        val resolved = resolveLanguage(language)
        return maps.bulkLoadPrefixes[resolved] ?: emptyList()
    }

    /**
     * Returns full-scan methods for a specific language.
     * These are methods that iterate the full collection (groupBy, distinct, toMap, etc.).
     */
    public fun fullScanMethods(language: Language): Set<String> {
        val resolved = resolveLanguage(language)
        return maps.fullScan[resolved] ?: emptySet()
    }

    /**
     * Returns IO methods for a specific language.
     * These are network, database, or file operations that should not appear inside loops.
     */
    public fun ioMethods(language: Language): Set<String> {
        val resolved = resolveLanguage(language)
        return maps.io[resolved] ?: emptySet()
    }

    /**
     * Returns true if the method name is a known O(1) factory or return-type method.
     * These are methods like setOf(), keySet(), toSet() that produce O(1) collection types.
     */
    public fun isO1Factory(
        language: Language,
        methodName: String,
    ): Boolean {
        val resolved = resolveLanguage(language)
        return maps.o1Factory[resolved]?.contains(methodName) == true
    }

    /**
     * Returns true if the method name is a known memoization/caching method for the language.
     */
    public fun isMemoizationMethod(
        language: Language,
        methodName: String,
    ): Boolean {
        val resolved = resolveLanguage(language)
        return maps.memoization[resolved]?.contains(methodName) == true
    }

    /**
     * Returns true if the accessor name is a known tree-traversal accessor for the language.
     */
    public fun isTreeTraversalAccessor(
        language: Language,
        accessorName: String,
    ): Boolean {
        val resolved = resolveLanguage(language)
        return maps.treeTraversal[resolved]?.contains(accessorName) == true
    }

    /**
     * Returns true if the name is a known visited-set variable name for the language.
     */
    public fun isVisitedSetName(
        language: Language,
        name: String,
    ): Boolean {
        val resolved = resolveLanguage(language)
        return maps.visitedSetNames[resolved]?.contains(name.lowercase()) == true
    }

    // --- Purity classification ---

    /**
     * Returns pure-method prefixes for the given language.
     */
    public fun purePrefixes(language: Language): List<String> {
        val resolved = resolveLanguage(language)
        return maps.purePrefixes[resolved] ?: emptyList()
    }

    /**
     * Returns side-effect prefixes for the given language.
     */
    public fun sideEffectPrefixes(language: Language): List<String> {
        val resolved = resolveLanguage(language)
        return maps.sideEffectPrefixes[resolved] ?: emptyList()
    }

    /**
     * Returns side-effect targets for the given language.
     */
    public fun sideEffectTargets(language: Language): List<String> {
        val resolved = resolveLanguage(language)
        return maps.sideEffectTargets[resolved] ?: emptyList()
    }

    // --- String detection ---

    /**
     * Returns string indicator method names for the given language.
     */
    public fun stringIndicators(language: Language): Set<String> {
        val resolved = resolveLanguage(language)
        return maps.stringIndicators[resolved] ?: emptySet()
    }

    /**
     * Returns string name suffixes for the given language.
     */
    public fun stringNameSuffixes(language: Language): Set<String> {
        val resolved = resolveLanguage(language)
        return maps.stringNameSuffixes[resolved] ?: emptySet()
    }

    /**
     * Returns string exact names for the given language.
     */
    public fun stringExactNames(language: Language): Set<String> {
        val resolved = resolveLanguage(language)
        return maps.stringExactNames[resolved] ?: emptySet()
    }

    // --- Monadic type detection ---

    /**
     * Returns true if the target expression matches a monadic type or variable name
     * across all languages.
     */
    public fun isMonadicTarget(targetText: String): Boolean {
        val allTypes =
            maps.monadicTypes.values
                .flatten()
                .toSet()
        val allVarNames =
            maps.monadicVarNames.values
                .flatten()
                .toSet()
        return allTypes.any { containsTypeReference(targetText, it) } ||
            matchesCamelCasePrefix(extractBaseVarName(targetText), allVarNames) ||
            extractBaseVarName(targetText) in allVarNames
    }

    /**
     * Returns true if the target expression matches a monadic type or variable name
     * for the given language, or references a class from `monadic-factory-classes`
     * (e.g. `ReactiveSecurityContextHolder.getContext()`) - a reactive factory whose
     * methods return Mono/Flux, not a collection to iterate.
     */
    public fun isMonadicTarget(
        language: Language,
        targetText: String,
    ): Boolean {
        val resolved = resolveLanguage(language)
        val types = maps.monadicTypes[resolved] ?: emptySet()
        val varNames = maps.monadicVarNames[resolved] ?: emptySet()
        return types.any { containsTypeReference(targetText, it) } ||
            matchesCamelCasePrefix(extractBaseVarName(targetText), varNames) ||
            extractBaseVarName(targetText) in varNames ||
            isReactiveFactoryTarget(targetText, language)
    }

    // ReactiveSecurityContextHolder.getContext().map(...).flatMap(...) — the class is a
    // reactive factory whose methods return Mono/Flux, not a collection to iterate. Boundary-
    // checked, not just a bare contains(), so an unrelated class merely containing one of these
    // names doesn't false-match - though a wrapper/adapter class like "LegacyServerRequestAdapter"
    // still would (its capitalized "Adapter" tail satisfies the trailing boundary the same way
    // "ResultSet"/"DateUtils" do elsewhere in this campaign); narrow enough that it's left as a
    // documented gap rather than a YAML exclusion list, same call as ExpensiveSortComparatorRule.
    private fun isReactiveFactoryTarget(
        target: String,
        language: Language,
    ): Boolean {
        val factories = extraSection(language, "monadic-factory-classes")
        if (factories.isEmpty()) return false
        return containsAnyAtWordBoundary(target, factories)
    }

    /**
     * Returns the bulk-alternative method name for a single-record operation.
     * e.g. for "findById" → "findAllById" (Spring Data).
     */
    public fun bulkAlternative(
        language: Language,
        methodName: String,
    ): String? {
        val resolved = resolveLanguage(language)
        return maps.bulkAlternatives[resolved]?.get(methodName)
    }

    /**
     * Returns all bulk alternatives for the language as a map (single → bulk).
     */
    public fun bulkAlternatives(language: Language): Map<String, String> {
        val resolved = resolveLanguage(language)
        return maps.bulkAlternatives[resolved] ?: emptyMap()
    }

    /** Generic query for extra YAML sections not covered by explicit fields. */
    public fun extraSection(
        language: Language,
        key: String,
    ): Set<String> {
        val resolved = resolveLanguage(language)
        return maps.extras[resolved]?.get(key) ?: emptySet()
    }

    // ── Rule-specific convenience methods (all delegate to extraSection) ──

    public fun hiddenLoopSkipMethods(language: Language): Set<String> = extraSection(language, "hidden-loop-skip-methods")

    public fun hiddenLoopSkipPrefixes(language: Language): Set<String> = extraSection(language, "hidden-loop-skip-prefixes")

    public fun hiddenLoopSkipKeywords(language: Language): Set<String> = extraSection(language, "hidden-loop-skip-keywords")

    public fun staticUtilityClasses(language: Language): Set<String> = extraSection(language, "static-utility-classes")

    public fun nonListTargetsExact(language: Language): Set<String> = extraSection(language, "non-list-targets-exact")

    public fun nonListTargetsSuffixes(language: Language): Set<String> = extraSection(language, "non-list-targets-suffixes")

    public fun nonListTargetsContains(language: Language): Set<String> = extraSection(language, "non-list-targets-contains")

    public fun bulkRemovalMethods(language: Language): Set<String> = extraSection(language, "bulk-removal-methods")

    public fun nonGrowthMutations(language: Language): Set<String> = extraSection(language, "non-growth-mutations")

    public fun scalarAccumulationMethods(language: Language): Set<String> = extraSection(language, "scalar-accumulation-methods")

    public fun stringBuildingMethods(language: Language): Set<String> = extraSection(language, "string-building-methods")

    public fun keyedAggregationMethods(language: Language): Set<String> = extraSection(language, "keyed-aggregation-methods")

    public fun scalarReceiverHints(language: Language): Set<String> = extraSection(language, "scalar-receiver-hints")

    public fun smallCollectionHints(language: Language): Set<String> = extraSection(language, "small-collection-hints")

    public fun mapValueAccessors(language: Language): Set<String> = extraSection(language, "map-value-accessors")

    public fun singleFetchPrefixes(language: Language): Set<String> = extraSection(language, "single-fetch-prefixes")

    public fun batchMethodSuffixes(language: Language): Set<String> = extraSection(language, "batch-method-suffixes")

    public fun batchMethodPrefixes(language: Language): Set<String> = extraSection(language, "batch-method-prefixes")

    public fun repositoryPatterns(language: Language): Set<String> = extraSection(language, "repository-patterns")

    public fun dateTypeNames(language: Language): Set<String> = extraSection(language, "date-type-names")

    public fun dateParseMethods(language: Language): Set<String> = extraSection(language, "date-parse-methods")

    public fun dateParseTargets(language: Language): Set<String> = extraSection(language, "date-parse-targets")

    public fun domTargetNames(language: Language): Set<String> = extraSection(language, "dom-target-names")

    public fun nonDeterministicMethods(language: Language): Set<String> = extraSection(language, "non-deterministic-methods")

    public fun futureIndicators(language: Language): Set<String> = extraSection(language, "future-indicators")

    public fun collectionGetterNames(language: Language): Set<String> = extraSection(language, "collection-getter-names")

    public fun scalarSuffixes(language: Language): Set<String> = extraSection(language, "scalar-suffixes")

    public fun nonRepositoryTargets(language: Language): Set<String> = extraSection(language, "non-repository-targets")

    public fun nonIoTargets(language: Language): Set<String> = extraSection(language, "non-io-targets")

    public fun ioMethodCandidates(language: Language): Set<String> = extraSection(language, "io-method-candidates")

    public fun ioTargetPatterns(language: Language): Set<String> = extraSection(language, "io-target-patterns")

    public fun getterExcludedNames(language: Language): Set<String> = extraSection(language, "getter-excluded-names")

    public fun nonRegexMatchesTargets(language: Language): Set<String> = extraSection(language, "non-regex-matches-targets")

    public fun implicitIterationOps(language: Language): Set<String> = extraSection(language, "implicit-iteration-ops")

    public fun hofMethods(language: Language): Set<String> = extraSection(language, "hof-methods")

    public fun filterMethods(language: Language): Set<String> = extraSection(language, "filter-methods")

    public fun streamEntryMethods(language: Language): Set<String> = extraSection(language, "stream-entry-methods")

    public fun objectMethods(language: Language): Set<String> = extraSection(language, "object-methods")

    public fun reflectionExclusions(language: Language): Set<String> = extraSection(language, "reflection-exclusions")

    public fun typeCheckPrefixes(language: Language): Set<String> = extraSection(language, "type-check-prefixes")

    public fun sequentialReadPrefixes(language: Language): Set<String> = extraSection(language, "sequential-read-prefixes")

    /**
     * Looks up a method's [LookupKind] for a specific language.
     * Returns null if the method has no lookup classification in that language.
     */
    public fun lookupKindFor(
        methodName: String,
        language: Language,
    ): LookupKind? {
        val resolved = resolveLanguage(language)
        return maps.methods[resolved]
            ?.get(methodName)
            ?.takeIf { it.category == SemanticCategory.LOOKUP }
            ?.lookupKind
    }

    /**
     * Looks up a method's [SortKind] for a specific language.
     * Returns null if the method has no sort classification in that language.
     */
    public fun sortKindFor(
        methodName: String,
        language: Language,
    ): SortKind? {
        val resolved = resolveLanguage(language)
        return maps.methods[resolved]
            ?.get(methodName)
            ?.takeIf { it.category == SemanticCategory.SORT }
            ?.sortKind
    }

    /**
     * Looks up a method's [AccessKind] for a specific language.
     * Returns null if the method has no access classification in that language.
     */
    public fun accessKindFor(
        methodName: String,
        language: Language,
    ): AccessKind? {
        val resolved = resolveLanguage(language)
        return maps.methods[resolved]
            ?.get(methodName)
            ?.takeIf { it.category == SemanticCategory.ACCESS }
            ?.accessKind
    }

    private fun resolveLanguage(language: Language): Language =
        when (language) {
            Language.TYPESCRIPT -> Language.JAVASCRIPT
            else -> language
        }

    public companion object {
        /** Cached singleton — avoids re-parsing ~5K lines of YAML on every call. */
        public val DEFAULT: LanguageSemanticsRegistry by lazy { loadDefaults() }

        private val LANGUAGE_FILES =
            mapOf(
                Language.JAVA to "semantics/java.yml",
                Language.GROOVY to "semantics/groovy.yml",
                Language.JAVASCRIPT to "semantics/javascript.yml",
                Language.KOTLIN to "semantics/kotlin.yml",
            )

        private const val FRAMEWORKS_INDEX = "semantics/frameworks-index.txt"

        /**
         * Loads the default registry from classpath YAML resources.
         */
        public fun loadDefaults(): LanguageSemanticsRegistry {
            val maps = LanguageMaps()

            for ((lang, resource) in LANGUAGE_FILES) {
                val text = loadResource(resource) ?: continue
                maps.merge(lang, parseYaml(text))
            }

            val frameworkCount = mergeFrameworkOverlays(maps)

            logger.info {
                val total = maps.methods.values.sumOf { it.size }
                "Language semantics registry loaded: $total methods across ${maps.methods.size} languages" +
                    if (frameworkCount > 0) " ($frameworkCount framework overlays)" else ""
            }

            return LanguageSemanticsRegistry(maps)
        }

        private fun mergeFrameworkOverlays(maps: LanguageMaps): Int {
            val index =
                loadResource(FRAMEWORKS_INDEX) ?: run {
                    logger.warn { "Framework index not found: $FRAMEWORKS_INDEX" }
                    return 0
                }
            val fileNames = index.lines().map { it.trim() }.filter { it.isNotEmpty() && !it.startsWith("#") }
            return fileNames.count { fileName -> mergeFrameworkFile(maps, fileName) }
        }

        private fun mergeFrameworkFile(
            maps: LanguageMaps,
            fileName: String,
        ): Boolean {
            val resource = "semantics/frameworks/$fileName"
            val text = loadResource(resource) ?: return false
            val lang =
                extractLanguageFromYaml(text) ?: run {
                    logger.warn { "No language: key in framework YAML $fileName — skipping" }
                    return false
                }
            maps.merge(lang, parseYaml(text))
            return true
        }

        /**
         * Returns a registry merged with user-provided overrides.
         */
        public fun withOverrides(
            base: LanguageSemanticsRegistry,
            userHeavyweightTypes: Set<String>,
        ): LanguageSemanticsRegistry {
            if (userHeavyweightTypes.isEmpty()) return base
            val newMaps = base.maps.copy()
            for (lang in newMaps.heavyweight.keys) {
                newMaps.heavyweight[lang] = newMaps.heavyweight[lang]!! + userHeavyweightTypes
            }
            for (lang in Language.entries) {
                if (lang !in newMaps.heavyweight) {
                    newMaps.heavyweight[lang] = userHeavyweightTypes
                }
            }
            return LanguageSemanticsRegistry(newMaps)
        }

        private fun loadResource(path: String): String? {
            val stream = LanguageSemanticsRegistry::class.java.classLoader.getResourceAsStream(path)
            if (stream == null) {
                logger.warn { "Semantics resource not found: $path" }
                return null
            }
            return stream.bufferedReader().readText()
        }
    }
}

private fun matchesCamelCasePrefix(
    varName: String,
    prefixes: Set<String>,
): Boolean =
    prefixes.any { prefix ->
        varName.length > prefix.length &&
            varName.startsWith(prefix) &&
            (varName[prefix.length].isUpperCase() || varName[prefix.length].isDigit())
    }

/**
 * Returns true if [typeName] matches one of [candidates] at a word boundary, unless it also
 * matches one of [exclusions] - real types whose name happens to contain a candidate as a
 * camelCase-capitalized tail without actually being that kind of type (`java.sql.ResultSet`
 * contains "Set" but isn't a `java.util.Set`; `java.io.InputStream`/`OutputStream` contain
 * "Stream" but aren't a collection `Stream`). A plain boundary check alone can't tell these
 * apart from a genuine subclass-style name like `UserHashMap` - both have a capitalized
 * candidate substring at the end of the string - so known collisions are excluded explicitly
 * via YAML (`non-collection-type-names`/`non-o1-type-names`) instead.
 */
private fun matchesTypeName(
    typeName: String,
    candidates: Set<String>?,
    exclusions: Set<String>,
): Boolean {
    if (candidates.isNullOrEmpty()) return false
    // ignoreCase = false: type names are case-sensitive identifiers (PascalCase by
    // convention), and some callers pass a receiver variable name here too - keeping
    // case-sensitive avoids matching a lowercase variable like "map" against a real "Map"
    // type name (see inferO1Factory in TypeEnvironment.kt, which relies on exactly this).
    // Candidates checked first: exclusions can only change the outcome once a candidate
    // already matched, so this short-circuits the (dominant, in a real codebase) no-match
    // case without ever scanning the exclusion list.
    if (!containsAnyAtWordBoundary(typeName, candidates, ignoreCase = false)) return false
    return !containsAnyAtWordBoundary(typeName, exclusions, ignoreCase = false)
}

/**
 * Returns true if [targetText] contains [type] as a whole identifier segment — the
 * character right before the match, if any, must not be a letter or digit, AND the match
 * must not run straight into a lowercase continuation of the same word on the far side
 * either. Plain `contains` would let a monadic type name like "Stream" match inside
 * "parallelStream()" too, treating every `.parallelStream()`/`.someStream()` call as
 * monadic and silently skipping it from loop classification. Same failure shape as the
 * camelCase-prefix bugs elsewhere in this campaign (a name matches textually without the
 * boundary that would prove it's actually that identifier).
 *
 * Deliberately NOT the same check as [com.github.tvinke.algorilla.util.containsAtWordBoundary]:
 * that helper also accepts an uppercase *matched* character as leading-boundary proof on its
 * own (so `"hibernateSession"` counts "Session" as a real word even though the preceding "e"
 * is a letter) — exactly the shape that would make "Stream" match again inside
 * "parallelStream()", since the "S" there is capitalized too. A type-literal reference like
 * `Stream.of(...)` has to be a standalone token, not a capitalized tail of a longer camelCase
 * name, so the leading side here only accepts start-of-string or a genuinely non-letter-or-
 * digit separator before the match.
 */
private fun containsTypeReference(
    targetText: String,
    type: String,
): Boolean {
    var idx = targetText.indexOf(type)
    while (idx >= 0) {
        val leadingOk = idx == 0 || !targetText[idx - 1].isLetterOrDigit()
        val afterIdx = idx + type.length
        val trailingOk =
            afterIdx >= targetText.length || !targetText[afterIdx].isLetterOrDigit() || targetText[afterIdx].isUpperCase()
        if (leadingOk && trailingOk) return true
        idx = targetText.indexOf(type, idx + 1)
    }
    return false
}

private fun extractBaseVarName(targetText: String): String {
    val cleaned = targetText.trim()
    val dotIdx = cleaned.indexOf('.')
    val base = if (dotIdx > 0) cleaned.substring(0, dotIdx) else cleaned
    return if (base == "this" && dotIdx > 0) {
        val rest = cleaned.substring(dotIdx + 1)
        val nextDot = rest.indexOf('.')
        if (nextDot > 0) rest.substring(0, nextDot) else rest
    } else {
        base
    }
}
