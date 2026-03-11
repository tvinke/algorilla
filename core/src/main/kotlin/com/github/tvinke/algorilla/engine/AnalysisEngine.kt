package com.github.tvinke.algorilla.engine

import com.github.tvinke.algorilla.cache.AnalysisCache
import com.github.tvinke.algorilla.cache.CachedFileEntry
import com.github.tvinke.algorilla.cache.CachedFinding
import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.CallGraphBuilder
import com.github.tvinke.algorilla.graph.ComplexityAnnotator
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.ObjectCreation
import com.github.tvinke.algorilla.model.VariableDecl
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.Rule
import com.github.tvinke.algorilla.semantics.CollectionSemanticsRegistry
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
    private val registry: CollectionSemanticsRegistry = createRegistry(config),
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

        val (irTrees, symbolTable, errors) = parseFiles(filesToParse)
        val callGraph = buildCallGraph(irTrees, symbolTable)
        annotateComplexity(symbolTable, callGraph)
        val rawFindings = evaluateRules(irTrees, symbolTable, callGraph)
        val deduplicated = deduplicateOverlappingRules(rawFindings)
        val freshFindings = SuppressionFilter().filter(deduplicated, irTrees, aliasIndex)

        val allFindings = freshFindings + cachedFindings
        saveCache(sourceFiles, filesToParse, freshFindings, cachedEntries)

        val elapsed = System.currentTimeMillis() - startTime
        logger.info { "Analysis complete: ${allFindings.size} findings in ${elapsed}ms" }

        return AnalysisResult(
            findings = allFindings.sortedWith(findingOrder),
            filesAnalyzed = sourceFiles.size,
            filesCached = sourceFiles.size - filesToParse.size,
            errors = errors,
            elapsedMs = elapsed,
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
 * When two rules flag the same location for overlapping reasons, keep the higher-confidence one.
 * Currently deduplicates: uncached-getter vs redundant-expensive-call (same call, same location).
 */
private fun deduplicateOverlappingRules(findings: List<Finding>): List<Finding> {
    data class LocationKey(
        val file: String,
        val line: Int,
    )

    val redundantLocations =
        findings
            .filter { it.ruleId == "redundant-expensive-call" }
            .map { LocationKey(it.location.file, it.location.line) }
            .toSet()

    return findings.filter { finding ->
        finding.ruleId != "uncached-getter" ||
            LocationKey(finding.location.file, finding.location.line) !in redundantLocations
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
 * Sort findings so the most actionable ones appear first:
 * severity (ERROR > WARNING > INFO), then category weight, then location.
 */
private val findingOrder: Comparator<Finding> =
    compareByDescending<Finding> { it.severity.ordinal }
        .thenBy { it.category?.ordinal ?: Int.MAX_VALUE }
        .thenBy { it.location.file }
        .thenBy { it.location.line }

private fun createRegistry(config: AnalysisConfig): CollectionSemanticsRegistry {
    val base = CollectionSemanticsRegistry.loadDefaults()
    return CollectionSemanticsRegistry.withOverrides(base, config.heavyweightTypes)
}
