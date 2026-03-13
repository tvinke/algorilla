package com.github.tvinke.algorilla.engine

import com.github.tvinke.algorilla.cache.AnalysisCache
import com.github.tvinke.algorilla.cache.CachedFileEntry
import com.github.tvinke.algorilla.cache.CachedFinding
import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.CallGraphBuilder
import com.github.tvinke.algorilla.graph.ComplexityAnnotator
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.model.Confidence
import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.LookupCall
import com.github.tvinke.algorilla.model.ObjectCreation
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.model.VariableDecl
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.Rule
import com.github.tvinke.algorilla.semantics.LanguageSemanticsRegistry
import com.github.tvinke.algorilla.util.findDescendants
import com.github.tvinke.algorilla.util.transform
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Orchestrates the four-pass analysis pipeline: parse, call graph construction,
 * complexity annotation, and rule evaluation. Supports incremental analysis
 * via file content hashing and an on-disk cache.
 */
public class AnalysisEngine(
    private val parsers: List<LanguageParser>,
    private val rules: List<Rule>,
    private val config: AnalysisConfig,
    private val cache: AnalysisCache? = null,
    @Suppress("UNUSED_PARAMETER") verbose: Boolean = false,
    private val registry: LanguageSemanticsRegistry = createRegistry(config),
) {
    /**
     * Runs the full analysis pipeline on the given source files and returns all findings.
     */
    private val aliasIndex: Map<String, String> =
        rules
            .flatMap { rule ->
                rule.aliases.map { alias -> alias to rule.id }
            }.toMap()

    public fun analyze(sourceFiles: List<String>): AnalysisResult {
        logger.info { "Starting analysis of ${sourceFiles.size} files" }
        val startTime = System.currentTimeMillis()

        val cachedEntries = cache?.load() ?: emptyMap()
        val (filesToParse, cachedFindings) = partitionByCache(sourceFiles, cachedEntries)

        logger.info { "Cache: ${sourceFiles.size - filesToParse.size} files unchanged, ${filesToParse.size} to analyze" }

        val (parsedTrees, _, errors) = parseFiles(filesToParse)
        val irTrees = markScalarLookups(parsedTrees)
        val symbolTable = rebuildSymbolTable(irTrees)
        val callGraph = buildCallGraph(irTrees, symbolTable)
        annotateComplexity(symbolTable, callGraph)
        val rawFindings = evaluateRules(irTrees, symbolTable, callGraph)
        val deduplicated = applySubsumption(rawFindings, rules)
        val confidenceAdjusted = adjustConfidence(deduplicated)
        val freshFindings = SuppressionFilter().filter(confidenceAdjusted, irTrees, aliasIndex)

        val allFindings = freshFindings + cachedFindings
        saveCache(sourceFiles, filesToParse, freshFindings, cachedEntries)

        val elapsed = System.currentTimeMillis() - startTime
        logger.info { "Analysis complete: ${allFindings.size} findings in ${elapsed}ms" }

        return buildResult(allFindings, sourceFiles.size, filesToParse.size, errors, elapsed)
    }

    private fun buildResult(
        allFindings: List<Finding>,
        totalFiles: Int,
        parsedFiles: Int,
        errors: List<AnalysisError>,
        elapsedMs: Long,
    ): AnalysisResult {
        val sorted = allFindings.sortedWith(findingOrder)
        val unfilteredCounts = sorted.groupingBy { it.severity }.eachCount()
        val unfilteredConfidenceCounts = sorted.groupingBy { it.confidence }.eachCount()
        val filtered =
            sorted
                .filter { it.severity >= config.minSeverity }
                .filter { it.confidence >= config.minConfidence }
        return AnalysisResult(
            findings = filtered,
            filesAnalyzed = totalFiles,
            filesCached = totalFiles - parsedFiles,
            errors = errors,
            elapsedMs = elapsedMs,
            unfilteredCounts = unfilteredCounts,
            unfilteredConfidenceCounts = unfilteredConfidenceCounts,
        )
    }

    private fun partitionByCache(
        sourceFiles: List<String>,
        cachedEntries: Map<String, CachedFileEntry>,
    ): Pair<List<String>, List<Finding>> {
        if (cachedEntries.isEmpty()) return Pair(sourceFiles, emptyList())

        val filesToParse = mutableListOf<String>()
        val cachedFindings = mutableListOf<Finding>()

        for (file in sourceFiles) {
            val entry = cachedEntries[file]
            if (entry != null && entry.contentHash == AnalysisCache.hashFile(file)) {
                cachedFindings.addAll(entry.findings.map { it.toFinding() })
            } else {
                filesToParse.add(file)
            }
        }
        return Pair(filesToParse, cachedFindings)
    }

    private fun saveCache(
        allFiles: List<String>,
        freshFiles: List<String>,
        freshFindings: List<Finding>,
        previousCache: Map<String, CachedFileEntry>,
    ) {
        if (cache == null) return

        val freshSet = freshFiles.toSet()
        val freshFindingsByFile = freshFindings.groupBy { it.location.file }
        val entries =
            allFiles.map { file ->
                if (file in freshSet) {
                    CachedFileEntry(
                        filePath = file,
                        contentHash = AnalysisCache.hashFile(file),
                        findings = (freshFindingsByFile[file] ?: emptyList()).map { CachedFinding.fromFinding(it) },
                    )
                } else {
                    previousCache[file] ?: CachedFileEntry(
                        filePath = file,
                        contentHash = AnalysisCache.hashFile(file),
                        findings = emptyList(),
                    )
                }
            }
        cache.save(entries)
    }

    private fun markScalarLookups(irTrees: Map<String, FileRoot>): Map<String, FileRoot> = markScalarLookups(irTrees, registry)

    private fun parseFiles(sourceFiles: List<String>): ParseResult {
        val irTrees = mutableMapOf<String, FileRoot>()
        val symbolTable = SymbolTable()
        val errors = mutableListOf<AnalysisError>()

        for (file in sourceFiles) {
            try {
                val parser = parsers.find { it.canParse(file) } ?: continue
                val tree = parser.parse(file)
                irTrees[file] = tree
                collectSymbols(tree.children, symbolTable)
            } catch (e: ParseException) {
                logger.warn { "Failed to parse $file: ${e.message}" }
                errors.add(AnalysisError.Parse(file, e.message ?: "Unknown parse error"))
            }
        }
        logger.info { "Pass 1 complete: ${irTrees.size} files parsed, ${symbolTable.size()} symbols" }
        return ParseResult(irTrees, symbolTable, errors)
    }

    private fun rebuildSymbolTable(irTrees: Map<String, FileRoot>): SymbolTable {
        val symbolTable = SymbolTable()
        for ((_, tree) in irTrees) {
            collectSymbols(tree.children, symbolTable)
        }
        return symbolTable
    }

    private fun buildCallGraph(
        irTrees: Map<String, FileRoot>,
        symbolTable: SymbolTable,
    ): CallGraph = CallGraphBuilder(symbolTable).build(irTrees)

    private fun annotateComplexity(
        symbolTable: SymbolTable,
        callGraph: CallGraph,
    ) {
        ComplexityAnnotator(symbolTable, callGraph, config.maxCallDepth).annotate()
    }

    private fun evaluateRules(
        irTrees: Map<String, FileRoot>,
        symbolTable: SymbolTable,
        callGraph: CallGraph,
    ): List<Finding> {
        val context = AnalysisContext(irTrees, symbolTable, callGraph, config, registry)
        return rules
            .filter { rule -> isRuleEnabled(rule) }
            .flatMap { rule -> rule.evaluate(context).map { it.copy(category = rule.category) } }
    }

    private fun isRuleEnabled(rule: Rule): Boolean {
        val overrides = config.ruleOverrides
        if (overrides[rule.id]?.enabled == false) return false
        return rule.aliases.none { overrides[it]?.enabled == false }
    }

    private fun collectSymbols(
        nodes: List<IRNode>,
        symbolTable: SymbolTable,
    ) {
        for (node in nodes) {
            if (node is FunctionDecl) {
                symbolTable.register(node)
                registerParameterTypes(node, symbolTable)
            }
            if (node is VariableDecl) {
                registerVariableType(node, symbolTable)
            }
            collectSymbols(node.children, symbolTable)
        }
    }

    private fun registerParameterTypes(
        decl: FunctionDecl,
        symbolTable: SymbolTable,
    ) {
        for (param in decl.parameters) {
            if (param.typeName != null) {
                symbolTable.registerType(param.name, param.typeName)
            }
        }
    }

    private fun registerVariableType(
        decl: VariableDecl,
        symbolTable: SymbolTable,
    ) {
        val type =
            decl.typeName
                ?: decl.children
                    .filterIsInstance<ObjectCreation>()
                    .firstOrNull()
                    ?.typeName
        if (type != null) {
            symbolTable.registerType(decl.name, type)
        }
    }
}

/**
 * Adjusts confidence per finding based on structural certainty, type evidence,
 * and language context. This is the bridge between Phase 1 (infrastructure) and
 * per-rule confidence tuning (Phase 3).
 *
 * Promotion to HIGH:
 * - Rules whose structural pattern is always an anti-pattern regardless of types.
 *
 * Demotion to LOW:
 * - Findings in JS/TS files where detection relies on name-based heuristics
 *   without type annotations (unless the rule is structurally certain).
 *
 * Cross-method findings (evidence spanning multiple files) stay at MEDIUM —
 * the called function might not behave as inferred.
 */
internal fun adjustConfidence(findings: List<Finding>): List<Finding> =
    findings.map { finding ->
        // Only adjust findings still at the default MEDIUM. If a rule has explicitly
        // set HIGH or LOW, respect that — the rule has more context than the engine.
        if (finding.confidence != Confidence.MEDIUM) return@map finding
        val adjusted = classifyConfidence(finding)
        if (adjusted != finding.confidence) finding.copy(confidence = adjusted) else finding
    }

private fun classifyConfidence(finding: Finding): Confidence {
    // Structural rules: always real regardless of types or context
    if (finding.ruleId in STRUCTURALLY_CERTAIN_RULES) return Confidence.HIGH

    // High-FP rules: heuristic-heavy, often fire on legitimate tree-walk/transform patterns
    if (finding.ruleId in HEURISTIC_HEAVY_RULES) return Confidence.LOW

    // Cross-method findings: evidence spans multiple files → MEDIUM (not promoted)
    val isCrossMethod =
        finding.evidence.any { it.location.file != finding.location.file }
    if (isCrossMethod) return Confidence.MEDIUM

    // JS/TS files without type info: name-based heuristics only → LOW
    if (isUntypedScript(finding.location.file)) return Confidence.LOW

    return Confidence.MEDIUM
}

// Rules where the detected pattern is always an anti-pattern, independent of
// receiver types, collection sizes, or runtime context.
private val STRUCTURALLY_CERTAIN_RULES =
    setOf(
        "string-concat-in-loop", // String += in loop is always O(n²)
        "sort-for-last", // Sorting to get min/max is always suboptimal
        "in-loop-collection-building", // Concat/spread in reduce is always quadratic
        "filter-after-sort", // Filtering after sort wastes the sort work
        "expensive-sort-comparator", // O(n log n) comparator in O(n log n) sort = O(n² log² n)
        "repeated-regex-in-loop", // Compiling regex per iteration is always wasteful
        "repeated-reflection-in-loop", // Reflection lookup per iteration is always wasteful
        "quadratic-removal", // List.remove(index) in loop is always O(n²)
        "sequential-async-join-in-loop", // Await in loop is always sequential
    )

// Rules with high false-positive rates due to heuristic-heavy detection.
// These are demoted to LOW so they're hidden at default --confidence medium,
// but still available with --confidence low for deep exploration.
private val HEURISTIC_HEAVY_RULES =
    setOf(
        "expensive-callback", // Fires on legitimate tree-walk/transform callbacks
        "chained-getters", // Often flags intentional navigation patterns
    )

private val UNTYPED_EXTENSIONS = setOf("js", "jsx", "ts", "tsx", "vue", "mjs", "cjs")

private fun isUntypedScript(filePath: String): Boolean {
    val ext = filePath.substringAfterLast('.', "").lowercase()
    return ext in UNTYPED_EXTENSIONS
}

/**
 * When two rules flag the same source location for overlapping reasons, keep the
 * more specific one. Rules declare which other rules they subsume via [Rule.subsumes].
 *
 * For each (file, line) group, if rule A fired there and rule B also fired there,
 * and A declares that it subsumes B, then B's finding is dropped.
 */
internal fun applySubsumption(
    findings: List<Finding>,
    rules: List<Rule>,
): List<Finding> {
    val subsumptionIndex: Map<String, Set<String>> =
        rules.filter { it.subsumes.isNotEmpty() }.associate { it.id to it.subsumes }
    if (subsumptionIndex.isEmpty()) return findings

    data class LocationKey(
        val file: String,
        val line: Int,
    )

    val byLocation = findings.groupBy { LocationKey(it.location.file, it.location.line) }
    val subsumed = mutableSetOf<Finding>()

    for ((_, group) in byLocation) {
        if (group.size < 2) continue
        val ruleIdsPresent = group.map { it.ruleId }.toSet()
        for (finding in group) {
            if (finding in subsumed) continue
            val dominated =
                subsumptionIndex.entries
                    .filter { (dominantId, _) -> dominantId in ruleIdsPresent && dominantId != finding.ruleId }
                    .any { (_, subs) -> finding.ruleId in subs }
            if (dominated) subsumed.add(finding)
        }
    }

    return findings.filter { it !in subsumed }
}

private data class ParseResult(
    val irTrees: Map<String, FileRoot>,
    val symbolTable: SymbolTable,
    val errors: List<AnalysisError>,
)

/**
 * Result of a complete analysis run.
 */
public data class AnalysisResult(
    val findings: List<Finding>,
    val filesAnalyzed: Int,
    val filesCached: Int = 0,
    val errors: List<AnalysisError>,
    val elapsedMs: Long,
    val unfilteredCounts: Map<Severity, Int> = emptyMap(),
    val unfilteredConfidenceCounts: Map<Confidence, Int> = emptyMap(),
)

/**
 * Errors that occurred during analysis but did not stop the scan.
 */
public sealed class AnalysisError {
    public data class Parse(
        val file: String,
        val message: String,
    ) : AnalysisError()

    public data class FileAccess(
        val file: String,
        val message: String,
    ) : AnalysisError()
}

/**
 * Exception thrown when a source file cannot be parsed.
 */
public class ParseException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

/**
 * Sort findings so the most trustworthy and actionable ones appear first:
 * confidence (HIGH > MEDIUM > LOW), then severity (ERROR > WARNING > INFO),
 * then category weight, then location.
 */
private val findingOrder: Comparator<Finding> =
    compareByDescending<Finding> { it.confidence.ordinal }
        .thenByDescending { it.severity.ordinal }
        .thenBy { it.category?.ordinal ?: Int.MAX_VALUE }
        .thenBy { it.location.file }
        .thenBy { it.location.line }

/**
 * Post-parse pass that refines [LookupCall] nodes based on declared variable types:
 * - Marks as `isScalar = true` when the target type is not a known collection (String, Optional, etc.)
 * - Promotes to `isO1 = true` when the target type is a known O(1) type (Set, Map, etc.)
 *
 * Uses both same-file field types and a cross-file field type registry (keyed by declaring class)
 * so that `this.field` references resolve even when the cross-method resolver follows calls
 * into other files.
 */
public fun markScalarLookups(
    irTrees: Map<String, FileRoot>,
    registry: LanguageSemanticsRegistry,
): Map<String, FileRoot> {
    val globalFieldTypes = collectGlobalFieldTypes(irTrees)
    // Merge all field types across all classes as a fallback for inherited fields.
    // Precedence: local file > declaring class > any class in scan scope.
    val allGlobalFields = globalFieldTypes.values.fold(emptyMap<String, String>()) { acc, m -> acc + m }
    val result = mutableMapOf<String, FileRoot>()
    for ((file, fileRoot) in irTrees) {
        val language = fileRoot.language
        val localFieldTypes = collectFieldTypes(fileRoot)
        val transformed =
            fileRoot.transform { node ->
                if (node is FunctionDecl) {
                    val classFields = node.declaringClass?.let { globalFieldTypes[it] } ?: emptyMap()
                    val mergedFields = allGlobalFields + classFields + localFieldTypes
                    markScalarLookupsInFunction(node, language, registry, mergedFields)
                } else {
                    node
                }
            }
        result[file] = transformed as FileRoot
    }
    return result
}

/**
 * Builds a cross-file field type registry: className → (fieldName → typeName).
 * For each file, determines the declaring class from FunctionDecl nodes and associates
 * all class-level VariableDecl types with that class.
 */
private fun collectGlobalFieldTypes(irTrees: Map<String, FileRoot>): Map<String, Map<String, String>> {
    val global = mutableMapOf<String, MutableMap<String, String>>()
    for ((_, fileRoot) in irTrees) {
        val classNames = fileRoot.findDescendants<FunctionDecl>().mapNotNull { it.declaringClass }.toSet()
        val fieldTypes = collectFieldTypes(fileRoot)
        for (className in classNames) {
            global.getOrPut(className) { mutableMapOf() }.putAll(fieldTypes)
        }
    }
    return global
}

/**
 * Collects type info from class-level field declarations (VariableDecl nodes that are
 * direct children of the FileRoot, not nested inside a FunctionDecl).
 */
private fun collectFieldTypes(fileRoot: FileRoot): Map<String, String> {
    val fieldTypes = mutableMapOf<String, String>()

    fun collectFromChildren(children: List<IRNode>) {
        for (node in children) {
            if (node is FunctionDecl) continue
            if (node is VariableDecl && node.typeName != null) {
                fieldTypes[node.name] = node.typeName
            }
            collectFromChildren(node.children)
        }
    }
    collectFromChildren(fileRoot.children)
    return fieldTypes
}

private fun buildTypeMap(
    fn: FunctionDecl,
    fieldTypes: Map<String, String>,
    language: com.github.tvinke.algorilla.model.Language,
    registry: LanguageSemanticsRegistry,
): Map<String, String> {
    val typeMap = mutableMapOf<String, String>()
    typeMap.putAll(fieldTypes)
    for (param in fn.parameters) {
        if (param.typeName != null) typeMap[param.name] = param.typeName
    }
    for (varDecl in fn.findDescendants<VariableDecl>()) {
        val type =
            varDecl.typeName
                ?: varDecl.children
                    .filterIsInstance<ObjectCreation>()
                    .firstOrNull()
                    ?.typeName
                ?: inferO1Type(varDecl, language, registry)
        if (type != null) typeMap[varDecl.name] = type
    }
    return typeMap
}

/**
 * Infers an O(1) type name when a variable is initialized via a factory method or
 * instance method that returns a Set or Map. Uses the YAML-driven `o1-factory-methods`
 * section from LanguageSemanticsRegistry, plus a qualifier check against `o1-types`.
 */
private fun inferO1Type(
    varDecl: VariableDecl,
    language: com.github.tvinke.algorilla.model.Language,
    registry: LanguageSemanticsRegistry,
): String? {
    val call = varDecl.children.filterIsInstance<FunctionCall>().firstOrNull() ?: return null
    // Check if the qualified target is an O(1) type: Set.of(), Map.of(), ConcurrentHashMap.newKeySet()
    val target = call.qualifiedTarget
    if (target != null && registry.isO1Type(target)) return target
    // Check if the method name is a known O(1) factory/return-type method (YAML-driven)
    if (registry.isO1Factory(language, call.name)) return "Set"
    return null
}

private fun markScalarLookupsInFunction(
    fn: FunctionDecl,
    language: com.github.tvinke.algorilla.model.Language,
    registry: LanguageSemanticsRegistry,
    fieldTypes: Map<String, String>,
): FunctionDecl {
    val typeMap = buildTypeMap(fn, fieldTypes, language, registry)
    return fn.transform { node ->
        if (node is LookupCall && node.targetVariable != null && !node.isO1) {
            val varName = node.targetVariable.removePrefix("this.")
            val declaredType = typeMap[varName]
            when {
                declaredType == null -> node
                registry.isO1Type(language, declaredType) -> node.copy(isO1 = true)
                !registry.isCollectionType(language, declaredType) -> node.copy(isScalar = true)
                else -> node
            }
        } else {
            node
        }
    } as FunctionDecl
}

private fun createRegistry(config: AnalysisConfig): LanguageSemanticsRegistry {
    val base = LanguageSemanticsRegistry.loadDefaults()
    return LanguageSemanticsRegistry.withOverrides(base, config.heavyweightTypes)
}
