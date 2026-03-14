package com.github.tvinke.algorilla.engine

import com.github.tvinke.algorilla.cache.AnalysisCache
import com.github.tvinke.algorilla.cache.CachedFileEntry
import com.github.tvinke.algorilla.cache.CachedFinding
import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.CallGraphBuilder
import com.github.tvinke.algorilla.graph.ComplexityAnnotator
import com.github.tvinke.algorilla.graph.ParameterFlowAnnotator
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.model.Confidence
import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
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
@Suppress("LargeClass", "TooManyFunctions") // Pipeline orchestrator — each pass is a method
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
        annotateParameterFlows(irTrees, symbolTable)
        annotateComplexity(symbolTable, callGraph)
        val rawFindings = evaluateRules(irTrees, symbolTable, callGraph)
        val deduplicated = applySubsumption(rawFindings, rules)
        val vendoredDemoted = demoteVendoredCode(deduplicated)
        val fileLanguages = irTrees.mapValues { (_, root) -> root.language }
        val ruleIndex = rules.associateBy { it.id }
        val confidenceAdjusted = adjustConfidence(vendoredDemoted, fileLanguages, ruleIndex)
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

    private fun annotateParameterFlows(
        irTrees: Map<String, FileRoot>,
        symbolTable: SymbolTable,
    ) {
        ParameterFlowAnnotator(symbolTable).annotate(irTrees)
    }

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
 * Demotes findings in vendored/generated code to LOW confidence.
 * Vendored libraries (e.g. Spring's embedded ASM, cglib) produce noise from rules
 * designed for application code. The patterns are technically correct but not actionable.
 */
internal fun demoteVendoredCode(findings: List<Finding>): List<Finding> =
    findings.map { finding ->
        if (isVendoredCode(finding.location.file)) {
            finding.copy(confidence = Confidence.LOW)
        } else {
            finding
        }
    }

/** Known vendored/generated package path fragments. */
private val VENDORED_PATH_PATTERNS =
    listOf(
        // Spring Framework vendors ASM and cglib
        "/asm/",
        "/cglib/",
        // Vendored code markers
        "/internal/shaded/",
        "/shaded/",
        "/vendor/",
        "/thirdparty/",
        "/repackaged/",
        // Generated code
        "/generated/",
        "/generated-sources/",
    )

private fun isVendoredCode(filePath: String): Boolean = VENDORED_PATH_PATTERNS.any { filePath.contains(it) }

/**
 * Adjusts confidence per finding based on the rule's declared [Rule.defaultConfidence],
 * type evidence, and language context.
 *
 * The rule's [defaultConfidence] is the baseline. The engine may further adjust:
 * - Demote to LOW when the rule requires type context but the language lacks type declarations.
 * - Cap at MEDIUM for cross-method findings (evidence spanning multiple files).
 * - Demote to LOW for non-HIGH rules in languages without type declarations.
 */
internal fun adjustConfidence(
    findings: List<Finding>,
    fileLanguages: Map<String, Language>,
    ruleIndex: Map<String, Rule>,
): List<Finding> =
    findings.map { finding ->
        val language = fileLanguages[finding.location.file]
        val rule = ruleIndex[finding.ruleId]
        val ceiling = rule?.defaultConfidence ?: Confidence.MEDIUM

        // Step 1: Set baseline from rule's defaultConfidence for MEDIUM findings (promotion/demotion).
        //         Cap non-MEDIUM findings at the ceiling (prevents LOW rules from emitting HIGH).
        val baselined =
            when {
                finding.confidence == Confidence.MEDIUM -> ceiling
                finding.confidence.ordinal > ceiling.ordinal -> ceiling
                else -> finding.confidence
            }

        // Step 2: Apply language/evidence-based demotions (never promotes).
        val adjusted = applyDemotions(baselined, finding, language, rule)
        if (adjusted != finding.confidence) finding.copy(confidence = adjusted) else finding
    }

/**
 * Applies language and evidence-based demotions. Never promotes — only lowers confidence
 * when the evidence is weaker than the baseline suggests.
 */
private fun applyDemotions(
    confidence: Confidence,
    finding: Finding,
    language: Language?,
    rule: Rule?,
): Confidence {
    // Demote when rule requires type context but language lacks type declarations
    val lacksTypes = language != null && !language.hasTypeDeclarations
    if (lacksTypes && rule?.requiresTypeContext == true) return Confidence.LOW

    // Cross-method findings: cap at MEDIUM (inference across files is less certain)
    val isCrossMethod = finding.evidence.any { it.location.file != finding.location.file }
    if (isCrossMethod && confidence.ordinal > Confidence.MEDIUM.ordinal) return Confidence.MEDIUM

    // Languages without type declarations and non-HIGH confidence: demote to LOW
    if (lacksTypes && confidence != Confidence.HIGH) return Confidence.LOW

    return confidence
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
                ?: inferChainEndType(varDecl, language, registry)
                ?: inferFromMethodNameSuffix(varDecl)
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

/**
 * Chain-end type inference: when a variable is initialized via a stream pipeline,
 * the terminal operation determines the result type.
 * Examples: `.collect(Collectors.toSet())` → Set, `.toList()` → List.
 */
@Suppress("ReturnCount")
private fun inferChainEndType(
    varDecl: VariableDecl,
    language: com.github.tvinke.algorilla.model.Language,
    registry: LanguageSemanticsRegistry,
): String? {
    // Search ALL FunctionCall descendants for terminal operations
    val allCalls = varDecl.findDescendants<FunctionCall>()
    val terminal = allCalls.lastOrNull { it.name in TERMINAL_OPS } ?: return null

    // Direct terminal: .toSet(), .toList(), .toMap(), etc.
    if (registry.isO1Factory(language, terminal.name)) return "Set"
    if (terminal.name in LIST_TERMINALS) return "List"
    if (terminal.name == "toArray") return "Array"

    // Collector terminal: .collect(Collectors.toSet())
    if (terminal.name == "collect" && terminal.arguments.isNotEmpty()) {
        val collector = terminal.arguments.filterIsInstance<FunctionCall>().firstOrNull()
        if (collector != null) return inferCollectorType(collector)
    }
    return null
}

private val TERMINAL_OPS =
    setOf(
        "collect",
        "toList",
        "toSet",
        "toMap",
        "toMutableList",
        "toMutableSet",
        "toMutableMap",
        "toSortedSet",
        "toSortedMap",
        "toHashSet",
        "toArray",
        "toUnmodifiableList",
        "toUnmodifiableSet",
        "toUnmodifiableMap",
    )

private val LIST_TERMINALS = setOf("toList", "toMutableList", "toUnmodifiableList")

private fun inferCollectorType(collector: FunctionCall): String? =
    when (collector.name) {
        "toSet", "toUnmodifiableSet" -> "Set"
        "toList", "toUnmodifiableList" -> "List"
        "toMap", "toUnmodifiableMap", "toConcurrentMap" -> "Map"
        "groupingBy", "partitioningBy" -> "Map"
        else -> null
    }

/**
 * Method name suffix heuristic: last resort when no other inference succeeds.
 * Infers collection type from common method name patterns.
 */
@Suppress("CyclomaticComplexMethod")
private fun inferFromMethodNameSuffix(varDecl: VariableDecl): String? {
    val call =
        varDecl.children.filterIsInstance<FunctionCall>().lastOrNull()
            ?: varDecl.findDescendants<FunctionCall>().lastOrNull()
            ?: return null
    val name = call.name
    // Suffix must follow a meaningful prefix (not just "Set" alone)
    val minLen = "getX".length
    return when {
        name.endsWith("Stream") || name.endsWith("stream") -> "Stream"
        name.endsWith("Set") && name.length > minLen && name[0].isLowerCase() -> "Set"
        name.endsWith("Map") && name.length > minLen && name[0].isLowerCase() -> "Map"
        name.endsWith("List") && name.length > minLen && name[0].isLowerCase() -> "List"
        else -> null
    }
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
    val base = LanguageSemanticsRegistry.DEFAULT
    return LanguageSemanticsRegistry.withOverrides(base, config.heavyweightTypes)
}
