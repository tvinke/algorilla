package com.github.tvinke.algorilla.engine

import com.github.tvinke.algorilla.baseline.Baseline
import com.github.tvinke.algorilla.cache.AnalysisCache
import com.github.tvinke.algorilla.cache.CachedFileEntry
import com.github.tvinke.algorilla.cache.CachedFinding
import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.CallGraphBuilder
import com.github.tvinke.algorilla.graph.ComplexityAnnotator
import com.github.tvinke.algorilla.graph.LoopBoundAnnotator
import com.github.tvinke.algorilla.graph.ParameterFlowAnnotator
import com.github.tvinke.algorilla.graph.PathContextAnnotator
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.model.ClassNode
import com.github.tvinke.algorilla.model.Confidence
import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.ObjectCreation
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.model.VariableDecl
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.Rule
import com.github.tvinke.algorilla.rules.SuggestionContext
import com.github.tvinke.algorilla.semantics.LanguageSemanticsRegistry
import com.github.tvinke.algorilla.semantics.TypeEnvironment
import com.github.tvinke.algorilla.util.findDescendants
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Orchestrates the four-pass analysis pipeline: parse, call graph construction,
 * complexity annotation, and rule evaluation. Supports incremental analysis
 * via file content hashing and an on-disk cache.
 */
@Suppress("LargeClass", "TooManyFunctions") // Pipeline orchestrator — each analysis pass is a method
public class AnalysisEngine(
    private val parsers: List<LanguageParser>,
    private val rules: List<Rule>,
    private val config: AnalysisConfig,
    private val cache: AnalysisCache? = null,
    // Reserved for future per-pass logging control
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

    @Suppress("LongMethod")
    public fun analyze(sourceFiles: List<String>): AnalysisResult {
        logger.info { "Starting analysis of ${sourceFiles.size} files" }
        val startTime = System.currentTimeMillis()

        val cachedEntries = cache?.load() ?: emptyMap()
        val (filesToParse, cachedFindings) = partitionByCache(sourceFiles, cachedEntries)

        logger.info { "Cache: ${sourceFiles.size - filesToParse.size} files unchanged, ${filesToParse.size} to analyze" }

        val (parsedTrees, _, errors) = parseFiles(filesToParse)
        val fileContexts = buildInitialFileContexts(parsedTrees)
        val (irTrees, typeEnvironments) = markScalarLookups(parsedTrees, registry)
        val enrichedContexts = enrichFileContextsFromTypes(fileContexts, irTrees)
        val symbolTable = rebuildSymbolTable(irTrees)
        val callGraph = buildCallGraph(irTrees, symbolTable)
        val finalContexts = enrichFileContextsFromCallGraph(enrichedContexts, callGraph, irTrees)
        annotateLoopBounds(irTrees)
        annotateRecursion(irTrees)
        annotateParameterFlows(irTrees, symbolTable)
        annotateComplexity(symbolTable, callGraph)
        annotatePathContext(irTrees, callGraph)
        val rawFindings = evaluateRules(irTrees, symbolTable, callGraph, typeEnvironments, finalContexts)
        val deduplicated = applySubsumption(rawFindings, rules)
        val vendoredDemoted = demoteVendoredCode(deduplicated)
        val constructorDemoted = demoteConstructorFindings(vendoredDemoted, irTrees)
        val lifecycleDemoted = demoteLifecycleFindings(constructorDemoted, irTrees, registry)
        val fileLanguages = irTrees.mapValues { (_, root) -> root.language }
        val ruleIndex = rules.associateBy { it.id }
        val confidenceAdjusted = adjustConfidence(lifecycleDemoted, fileLanguages, ruleIndex)
        val suppressed = SuppressionFilter().filter(confidenceAdjusted, irTrees, aliasIndex)
        val pathContextEnriched = enrichPathContext(suppressed, irTrees)
        val freshFindings = renderCodeSuggestions(pathContextEnriched, finalContexts, fileLanguages)

        val allFindings =
            (freshFindings + cachedFindings).distinctBy { finding ->
                Baseline.fingerprintOf(finding).contentHash
            }
        saveCache(sourceFiles, filesToParse, freshFindings, cachedEntries, finalContexts)

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
        fileContexts: Map<String, FileContext> = emptyMap(),
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
                        fileContext = fileContexts[file],
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

    private fun annotateLoopBounds(irTrees: Map<String, FileRoot>) {
        LoopBoundAnnotator(registry).annotate(irTrees)
    }

    private fun annotateParameterFlows(
        irTrees: Map<String, FileRoot>,
        symbolTable: SymbolTable,
    ) {
        ParameterFlowAnnotator(symbolTable).annotate(irTrees)
    }

    private fun annotateRecursion(irTrees: Map<String, FileRoot>) {
        for ((_, root) in irTrees) {
            root.findDescendants<FunctionDecl>().forEach { fn ->
                fn.isRecursive = fn.findDescendants<com.github.tvinke.algorilla.model.FunctionCall>().any { it.name == fn.name }
            }
        }
    }

    private fun annotateComplexity(
        symbolTable: SymbolTable,
        callGraph: CallGraph,
    ) {
        ComplexityAnnotator(symbolTable, callGraph, config.maxCallDepth).annotate()
    }

    private fun annotatePathContext(
        irTrees: Map<String, FileRoot>,
        callGraph: CallGraph,
    ) {
        PathContextAnnotator(registry, callGraph).annotate(irTrees)
    }

    private fun evaluateRules(
        irTrees: Map<String, FileRoot>,
        symbolTable: SymbolTable,
        callGraph: CallGraph,
        typeEnvironments: Map<String, TypeEnvironment>,
        fileContexts: Map<String, FileContext> = emptyMap(),
    ): List<Finding> {
        val context = AnalysisContext(irTrees, symbolTable, callGraph, config, registry, typeEnvironments, fileContexts)
        return rules
            .filter { rule -> isRuleEnabled(rule) }
            .flatMap { rule -> rule.evaluate(context).map { it.copy(category = rule.category) } }
    }

    private fun isRuleEnabled(rule: Rule): Boolean {
        val overrides = config.ruleOverrides
        if (overrides[rule.id]?.enabled == false) return false
        return rule.aliases.none { overrides[it]?.enabled == false }
    }

    private fun renderCodeSuggestions(
        findings: List<Finding>,
        fileContexts: Map<String, FileContext>,
        fileLanguages: Map<String, com.github.tvinke.algorilla.model.Language>,
    ): List<Finding> =
        findings.map { finding ->
            if (finding.confidence == Confidence.LOW || finding.suggestedCode != null) {
                return@map finding
            }
            val lang = fileLanguages[finding.location.file] ?: return@map finding
            val fc = fileContexts[finding.location.file]
            val ctx =
                SuggestionContext(
                    language = lang,
                    imports = fc?.imports ?: emptySet(),
                    bulkAlternatives = registry.bulkAlternatives(lang),
                    detectedFrameworks = fc?.detectedFrameworks ?: emptySet(),
                )
            val code = finding.suggestions.firstNotNullOfOrNull { it.renderCode(ctx) }
            if (code != null) finding.copy(suggestedCode = code) else finding
        }

    private fun buildInitialFileContexts(irTrees: Map<String, FileRoot>): Map<String, FileContext> =
        irTrees.mapValues { (file, root) ->
            val imports = collectImports(file)
            val classNames =
                root
                    .findDescendants<ClassNode>()
                    .map { it.name }
                    .toSet()
            FileContext(
                filePath = file,
                language = root.language.name,
                imports = imports,
                classNames = classNames,
                detectedFrameworks = FrameworkDetector.detect(imports),
            )
        }

    private fun collectImports(filePath: String): Set<String> {
        val imports = mutableSetOf<String>()
        try {
            java.io.File(filePath).useLines { lines ->
                lines.take(IMPORT_SCAN_LINES).forEach { line ->
                    val trimmed = line.trimStart()
                    if (trimmed.startsWith("import ") || trimmed.startsWith("from ")) {
                        imports.add(trimmed)
                    }
                }
            }
        } catch (_: Exception) {
            // File might have been deleted between scan and parse
        }
        return imports
    }

    private fun enrichFileContextsFromTypes(
        contexts: Map<String, FileContext>,
        irTrees: Map<String, FileRoot>,
    ): Map<String, FileContext> =
        contexts.mapValues { (file, ctx) ->
            val root = irTrees[file] ?: return@mapValues ctx
            val exported = mutableMapOf<String, String>()
            root
                .findDescendants<FunctionDecl>()
                .filter { !it.isConstructor && it.returnType != null && it.declaringClass != null }
                .forEach { fn -> exported["${fn.declaringClass}.${fn.name}"] = fn.returnType!! }
            ctx.copy(exportedTypes = exported)
        }

    private fun enrichFileContextsFromCallGraph(
        contexts: Map<String, FileContext>,
        callGraph: CallGraph,
        irTrees: Map<String, FileRoot>,
    ): Map<String, FileContext> {
        val fnToFile = mutableMapOf<String, String>()
        for ((file, root) in irTrees) {
            root.findDescendants<FunctionDecl>().forEach { fn ->
                fnToFile[fn.qualifiedName] = file
            }
        }
        val fileDeps = mutableMapOf<String, MutableSet<String>>()
        for ((caller, callees) in callGraph.allEdges()) {
            val callerFile = fnToFile[caller] ?: continue
            for (callee in callees) {
                val calleeFile = fnToFile[callee] ?: continue
                if (callerFile != calleeFile) {
                    fileDeps.getOrPut(callerFile) { mutableSetOf() }.add(calleeFile)
                }
            }
        }
        return contexts.mapValues { (file, ctx) ->
            val deps = fileDeps[file]
            if (deps != null) ctx.copy(dependsOn = deps) else ctx
        }
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
    val projectRoot: java.io.File? = null,
    val acceptedCount: Int = 0,
    val baselinedCount: Int = 0,
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

private const val IMPORT_SCAN_LINES = 50

private fun createRegistry(config: AnalysisConfig): LanguageSemanticsRegistry {
    val base = LanguageSemanticsRegistry.DEFAULT
    return LanguageSemanticsRegistry.withOverrides(base, config.heavyweightTypes)
}
