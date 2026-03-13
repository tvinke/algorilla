package com.github.tvinke.algorilla.semantics

import com.github.tvinke.algorilla.model.AccessKind
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LookupKind
import com.github.tvinke.algorilla.model.SortKind
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Data-driven registry that maps method names to their semantics per language.
 * Loaded from YAML resource files at startup, with optional user overrides from `.algorilla.yml`.
 *
 * This is the single source of truth for method classification. All hardcoded method name sets
 * in the codebase should derive from this registry instead of maintaining their own lists.
 */
@Suppress("TooManyFunctions", "LongParameterList", "LargeClass")
public class LanguageSemanticsRegistry private constructor(
    private val methodsByLanguage: Map<Language, Map<String, MethodSemantics>>,
    private val heavyweightByLanguage: Map<Language, Set<String>>,
    private val collectionTypesByLanguage: Map<Language, Set<String>>,
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
    private val regexRecompilationByLanguage: Map<Language, Set<String>>,
    private val mutationByLanguage: Map<Language, Set<String>>,
    private val removalByLanguage: Map<Language, Set<String>>,
    private val bulkLoadPrefixesByLanguage: Map<Language, List<String>>,
    private val fullScanByLanguage: Map<Language, Set<String>>,
    private val ioByLanguage: Map<Language, Set<String>>,
    private val o1FactoryByLanguage: Map<Language, Set<String>>,
    private val memoizationByLanguage: Map<Language, Set<String>>,
    private val treeTraversalByLanguage: Map<Language, Set<String>>,
    private val visitedSetNamesByLanguage: Map<Language, Set<String>>,
    private val purePrefixesByLanguage: Map<Language, List<String>>,
    private val sideEffectPrefixesByLanguage: Map<Language, List<String>>,
    private val sideEffectTargetsByLanguage: Map<Language, List<String>>,
    private val stringIndicatorsByLanguage: Map<Language, Set<String>>,
    private val stringNameSuffixesByLanguage: Map<Language, Set<String>>,
    private val stringExactNamesByLanguage: Map<Language, Set<String>>,
    private val monadicTypesByLanguage: Map<Language, Set<String>>,
    private val monadicVarNamesByLanguage: Map<Language, Set<String>>,
    private val extrasByLanguage: Map<Language, Map<String, Set<String>>> = emptyMap(),
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
     * Returns true if the given type name is a known collection type for the language.
     * Checks both `collection-types` and `o1-types` (the union of all collection-like types).
     */
    public fun isCollectionType(
        language: Language,
        typeName: String,
    ): Boolean {
        val resolved = resolveLanguage(language)
        return collectionTypesByLanguage[resolved]?.any { typeName.contains(it) } == true ||
            o1ByLanguage[resolved]?.any { typeName.contains(it) } == true
    }

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
     * Returns the union of all IO methods across all languages.
     */
    public fun allIoMethods(): Set<String> = ioByLanguage.values.flatten().toSet()

    /**
     * Returns the copy-on-modify methods for a specific language.
     */
    public fun copyOnModifyMethodsFor(language: Language): Set<String> {
        val resolved = resolveLanguage(language)
        return copyOnModifyByLanguage[resolved] ?: emptySet()
    }

    /**
     * Returns the union of all copy-on-modify methods across all languages.
     */
    public fun allCopyOnModifyMethods(): Set<String> = copyOnModifyByLanguage.values.flatten().toSet()

    /**
     * Returns the union of all regex types across all languages.
     */
    public fun allRegexTypes(): Set<String> = regexTypesByLanguage.values.flatten().toSet()

    /**
     * Returns the union of all regex recompilation methods across all languages.
     */
    public fun allRegexRecompilationMethods(): Set<String> = regexRecompilationByLanguage.values.flatten().toSet()

    /**
     * Returns regex recompilation methods for a specific language.
     */
    public fun regexRecompilationMethods(language: Language): Set<String> {
        val resolved = resolveLanguage(language)
        return regexRecompilationByLanguage[resolved] ?: emptySet()
    }

    /**
     * Returns the union of all mutation methods across all languages.
     */
    public fun allMutationMethods(): Set<String> = mutationByLanguage.values.flatten().toSet()

    /**
     * Returns the union of all removal methods across all languages.
     */
    public fun allRemovalMethods(): Set<String> = removalByLanguage.values.flatten().toSet()

    /**
     * Returns removal methods for a specific language.
     */
    public fun removalMethods(language: Language): Set<String> {
        val resolved = resolveLanguage(language)
        return removalByLanguage[resolved] ?: emptySet()
    }

    /**
     * Returns the union of all bulk-load prefixes across all languages.
     */
    public fun allBulkLoadPrefixes(): List<String> =
        bulkLoadPrefixesByLanguage.values
            .flatten()
            .distinct()

    public fun bulkLoadPrefixes(language: Language): List<String> {
        val resolved = resolveLanguage(language)
        return bulkLoadPrefixesByLanguage[resolved] ?: emptyList()
    }

    /**
     * Returns full-scan methods for a specific language.
     * These are methods that iterate the full collection (groupBy, distinct, toMap, etc.).
     */
    public fun fullScanMethods(language: Language): Set<String> {
        val resolved = resolveLanguage(language)
        return fullScanByLanguage[resolved] ?: emptySet()
    }

    /**
     * Returns IO methods for a specific language.
     * These are network, database, or file operations that should not appear inside loops.
     */
    public fun ioMethods(language: Language): Set<String> {
        val resolved = resolveLanguage(language)
        return ioByLanguage[resolved] ?: emptySet()
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
        return o1FactoryByLanguage[resolved]?.contains(methodName) == true
    }

    /**
     * Returns true if the method name is a known memoization/caching method for the language.
     */
    public fun isMemoizationMethod(
        language: Language,
        methodName: String,
    ): Boolean {
        val resolved = resolveLanguage(language)
        return memoizationByLanguage[resolved]?.contains(methodName) == true
    }

    /**
     * Returns true if the accessor name is a known tree-traversal accessor for the language.
     */
    public fun isTreeTraversalAccessor(
        language: Language,
        accessorName: String,
    ): Boolean {
        val resolved = resolveLanguage(language)
        return treeTraversalByLanguage[resolved]?.contains(accessorName) == true
    }

    /**
     * Returns true if the name is a known visited-set variable name for the language.
     */
    public fun isVisitedSetName(
        language: Language,
        name: String,
    ): Boolean {
        val resolved = resolveLanguage(language)
        return visitedSetNamesByLanguage[resolved]?.contains(name.lowercase()) == true
    }

    // --- Purity classification ---

    /**
     * Returns pure-method prefixes for the given language.
     */
    public fun purePrefixes(language: Language): List<String> {
        val resolved = resolveLanguage(language)
        return purePrefixesByLanguage[resolved] ?: emptyList()
    }

    /**
     * Returns the union of all pure prefixes across all languages.
     */
    public fun allPurePrefixes(): List<String> =
        purePrefixesByLanguage.values
            .flatten()
            .distinct()

    /**
     * Returns side-effect prefixes for the given language.
     */
    public fun sideEffectPrefixes(language: Language): List<String> {
        val resolved = resolveLanguage(language)
        return sideEffectPrefixesByLanguage[resolved] ?: emptyList()
    }

    /**
     * Returns the union of all side-effect prefixes across all languages.
     */
    public fun allSideEffectPrefixes(): List<String> =
        sideEffectPrefixesByLanguage.values
            .flatten()
            .distinct()

    /**
     * Returns side-effect targets for the given language.
     */
    public fun sideEffectTargets(language: Language): List<String> {
        val resolved = resolveLanguage(language)
        return sideEffectTargetsByLanguage[resolved] ?: emptyList()
    }

    /**
     * Returns the union of all side-effect targets across all languages.
     */
    public fun allSideEffectTargets(): List<String> =
        sideEffectTargetsByLanguage.values
            .flatten()
            .distinct()

    // --- String detection ---

    /**
     * Returns string indicator method names for the given language.
     */
    public fun stringIndicators(language: Language): Set<String> {
        val resolved = resolveLanguage(language)
        return stringIndicatorsByLanguage[resolved] ?: emptySet()
    }

    /**
     * Returns the union of all string indicators across all languages.
     */
    public fun allStringIndicators(): Set<String> = stringIndicatorsByLanguage.values.flatten().toSet()

    /**
     * Returns string name suffixes for the given language.
     */
    public fun stringNameSuffixes(language: Language): Set<String> {
        val resolved = resolveLanguage(language)
        return stringNameSuffixesByLanguage[resolved] ?: emptySet()
    }

    /**
     * Returns the union of all string name suffixes across all languages.
     */
    public fun allStringNameSuffixes(): Set<String> = stringNameSuffixesByLanguage.values.flatten().toSet()

    /**
     * Returns string exact names for the given language.
     */
    public fun stringExactNames(language: Language): Set<String> {
        val resolved = resolveLanguage(language)
        return stringExactNamesByLanguage[resolved] ?: emptySet()
    }

    /**
     * Returns the union of all string exact names across all languages.
     */
    public fun allStringExactNames(): Set<String> = stringExactNamesByLanguage.values.flatten().toSet()

    // --- Monadic type detection ---

    /**
     * Returns true if the target expression matches a monadic type or variable name
     * across all languages.
     */
    public fun isMonadicTarget(targetText: String): Boolean {
        val allTypes = monadicTypesByLanguage.values.flatten().toSet()
        val allVarNames = monadicVarNamesByLanguage.values.flatten().toSet()
        return allTypes.any { targetText.contains(it) } ||
            matchesCamelCasePrefix(extractBaseVarName(targetText), allVarNames) ||
            extractBaseVarName(targetText) in allVarNames
    }

    /**
     * Returns true if the target expression matches a monadic type or variable name
     * for the given language.
     */
    public fun isMonadicTarget(
        language: Language,
        targetText: String,
    ): Boolean {
        val resolved = resolveLanguage(language)
        val types = monadicTypesByLanguage[resolved] ?: emptySet()
        val varNames = monadicVarNamesByLanguage[resolved] ?: emptySet()
        return types.any { targetText.contains(it) } ||
            matchesCamelCasePrefix(extractBaseVarName(targetText), varNames) ||
            extractBaseVarName(targetText) in varNames
    }

    /** Generic query for extra YAML sections not covered by explicit fields. */
    public fun extraSection(
        language: Language,
        key: String,
    ): Set<String> {
        val resolved = resolveLanguage(language)
        return extrasByLanguage[resolved]?.get(key) ?: emptySet()
    }

    /** Merges an extra section across all languages. */
    public fun allExtraSection(key: String): Set<String> = extrasByLanguage.values.flatMap { it[key] ?: emptySet() }.toSet()

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

    /**
     * Looks up a method's [LookupKind] for a specific language.
     * Returns null if the method has no lookup classification in that language.
     */
    public fun lookupKindFor(
        methodName: String,
        language: Language,
    ): LookupKind? {
        val resolved = resolveLanguage(language)
        return methodsByLanguage[resolved]
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
        return methodsByLanguage[resolved]
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
        return methodsByLanguage[resolved]
            ?.get(methodName)
            ?.takeIf { it.category == SemanticCategory.ACCESS }
            ?.accessKind
    }

    private fun resolveLanguage(language: Language): Language =
        when (language) {
            Language.TYPESCRIPT, Language.VUE -> Language.JAVASCRIPT
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
        @Suppress("LongMethod")
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

            return LanguageSemanticsRegistry(
                maps.methods,
                maps.heavyweight,
                maps.collectionTypes,
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
                maps.regexRecompilation,
                maps.mutation,
                maps.removal,
                maps.bulkLoadPrefixes,
                maps.fullScan,
                maps.io,
                maps.o1Factory,
                maps.memoization,
                maps.treeTraversal,
                maps.visitedSetNames,
                maps.purePrefixes,
                maps.sideEffectPrefixes,
                maps.sideEffectTargets,
                maps.stringIndicators,
                maps.stringNameSuffixes,
                maps.stringExactNames,
                maps.monadicTypes,
                maps.monadicVarNames,
                maps.extras,
            )
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
        @Suppress("LongMethod")
        public fun withOverrides(
            base: LanguageSemanticsRegistry,
            userHeavyweightTypes: Set<String>,
        ): LanguageSemanticsRegistry {
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
            return LanguageSemanticsRegistry(
                methodsByLanguage = base.methodsByLanguage,
                heavyweightByLanguage = merged,
                collectionTypesByLanguage = base.collectionTypesByLanguage,
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
                regexRecompilationByLanguage = base.regexRecompilationByLanguage,
                mutationByLanguage = base.mutationByLanguage,
                removalByLanguage = base.removalByLanguage,
                bulkLoadPrefixesByLanguage = base.bulkLoadPrefixesByLanguage,
                fullScanByLanguage = base.fullScanByLanguage,
                ioByLanguage = base.ioByLanguage,
                o1FactoryByLanguage = base.o1FactoryByLanguage,
                memoizationByLanguage = base.memoizationByLanguage,
                treeTraversalByLanguage = base.treeTraversalByLanguage,
                visitedSetNamesByLanguage = base.visitedSetNamesByLanguage,
                purePrefixesByLanguage = base.purePrefixesByLanguage,
                sideEffectPrefixesByLanguage = base.sideEffectPrefixesByLanguage,
                sideEffectTargetsByLanguage = base.sideEffectTargetsByLanguage,
                stringIndicatorsByLanguage = base.stringIndicatorsByLanguage,
                stringNameSuffixesByLanguage = base.stringNameSuffixesByLanguage,
                stringExactNamesByLanguage = base.stringExactNamesByLanguage,
                monadicTypesByLanguage = base.monadicTypesByLanguage,
                monadicVarNamesByLanguage = base.monadicVarNamesByLanguage,
                extrasByLanguage = base.extrasByLanguage,
            )
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

@Suppress("LongParameterList")
internal class LanguageMaps(
    val methods: MutableMap<Language, Map<String, MethodSemantics>> = mutableMapOf(),
    val heavyweight: MutableMap<Language, Set<String>> = mutableMapOf(),
    val collectionTypes: MutableMap<Language, Set<String>> = mutableMapOf(),
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
    val regexRecompilation: MutableMap<Language, Set<String>> = mutableMapOf(),
    val mutation: MutableMap<Language, Set<String>> = mutableMapOf(),
    val removal: MutableMap<Language, Set<String>> = mutableMapOf(),
    val bulkLoadPrefixes: MutableMap<Language, List<String>> = mutableMapOf(),
    val fullScan: MutableMap<Language, Set<String>> = mutableMapOf(),
    val io: MutableMap<Language, Set<String>> = mutableMapOf(),
    val o1Factory: MutableMap<Language, Set<String>> = mutableMapOf(),
    val memoization: MutableMap<Language, Set<String>> = mutableMapOf(),
    val treeTraversal: MutableMap<Language, Set<String>> = mutableMapOf(),
    val visitedSetNames: MutableMap<Language, Set<String>> = mutableMapOf(),
    val purePrefixes: MutableMap<Language, List<String>> = mutableMapOf(),
    val sideEffectPrefixes: MutableMap<Language, List<String>> = mutableMapOf(),
    val sideEffectTargets: MutableMap<Language, List<String>> = mutableMapOf(),
    val stringIndicators: MutableMap<Language, Set<String>> = mutableMapOf(),
    val stringNameSuffixes: MutableMap<Language, Set<String>> = mutableMapOf(),
    val stringExactNames: MutableMap<Language, Set<String>> = mutableMapOf(),
    val monadicTypes: MutableMap<Language, Set<String>> = mutableMapOf(),
    val monadicVarNames: MutableMap<Language, Set<String>> = mutableMapOf(),
    val extras: MutableMap<Language, MutableMap<String, Set<String>>> = mutableMapOf(),
) {
    @Suppress("CyclomaticComplexMethod", "LongMethod")
    fun merge(
        lang: Language,
        parsed: ParsedYaml,
    ) {
        methods[lang] = (methods[lang] ?: emptyMap()) + parsed.methods
        heavyweight[lang] = (heavyweight[lang] ?: emptySet()) + parsed.heavyweightTypes
        collectionTypes[lang] = (collectionTypes[lang] ?: emptySet()) + parsed.collectionTypes
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
        regexRecompilation[lang] = (regexRecompilation[lang] ?: emptySet()) + parsed.regexRecompilationMethods
        mutation[lang] = (mutation[lang] ?: emptySet()) + parsed.mutationMethods
        removal[lang] = (removal[lang] ?: emptySet()) + parsed.removalMethods
        bulkLoadPrefixes[lang] = ((bulkLoadPrefixes[lang] ?: emptyList()) + parsed.bulkLoadPrefixes).distinct()
        fullScan[lang] = (fullScan[lang] ?: emptySet()) + parsed.fullScanMethods
        io[lang] = (io[lang] ?: emptySet()) + parsed.ioMethods
        o1Factory[lang] = (o1Factory[lang] ?: emptySet()) + parsed.o1FactoryMethods
        memoization[lang] = (memoization[lang] ?: emptySet()) + parsed.memoizationMethods
        treeTraversal[lang] = (treeTraversal[lang] ?: emptySet()) + parsed.treeTraversalAccessors
        visitedSetNames[lang] = (visitedSetNames[lang] ?: emptySet()) + parsed.visitedSetNames
        purePrefixes[lang] = ((purePrefixes[lang] ?: emptyList()) + parsed.purePrefixes).distinct()
        sideEffectPrefixes[lang] = ((sideEffectPrefixes[lang] ?: emptyList()) + parsed.sideEffectPrefixes).distinct()
        sideEffectTargets[lang] = ((sideEffectTargets[lang] ?: emptyList()) + parsed.sideEffectTargets).distinct()
        stringIndicators[lang] = (stringIndicators[lang] ?: emptySet()) + parsed.stringIndicators
        stringNameSuffixes[lang] = (stringNameSuffixes[lang] ?: emptySet()) + parsed.stringNameSuffixes
        stringExactNames[lang] = (stringExactNames[lang] ?: emptySet()) + parsed.stringExactNames
        monadicTypes[lang] = (monadicTypes[lang] ?: emptySet()) + parsed.monadicTypes
        monadicVarNames[lang] = (monadicVarNames[lang] ?: emptySet()) + parsed.monadicVarNames
        // Merge all extra sections
        val langExtras = extras.getOrPut(lang) { mutableMapOf() }
        for ((key, values) in parsed.extras) {
            langExtras[key] = (langExtras[key] ?: emptySet()) + values
        }
    }
}

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

private fun matchesCamelCasePrefix(
    varName: String,
    prefixes: Set<String>,
): Boolean =
    prefixes.any { prefix ->
        varName.length > prefix.length &&
            varName.startsWith(prefix) &&
            (varName[prefix.length].isUpperCase() || varName[prefix.length].isDigit())
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
