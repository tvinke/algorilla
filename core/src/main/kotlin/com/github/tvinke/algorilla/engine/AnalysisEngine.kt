package com.github.tvinke.algorilla.engine

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.Rule
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Orchestrates the four-pass analysis pipeline: parse, call graph construction,
 * complexity annotation, and rule evaluation.
 */
public class AnalysisEngine(
    private val parsers: List<LanguageParser>,
    private val rules: List<Rule>,
    private val config: AnalysisConfig,
    @Suppress("UNUSED_PARAMETER") verbose: Boolean = false,
) {
    /**
     * Runs the full analysis pipeline on the given source files and returns all findings.
     */
    public fun analyze(sourceFiles: List<String>): AnalysisResult {
        logger.info { "Starting analysis of ${sourceFiles.size} files" }
        val startTime = System.currentTimeMillis()

        val (irTrees, symbolTable, errors) = parseFiles(sourceFiles)
        val callGraph = buildCallGraph()
        val rawFindings = evaluateRules(irTrees, symbolTable, callGraph)
        val findings = SuppressionFilter().filter(rawFindings, irTrees)

        val elapsed = System.currentTimeMillis() - startTime
        logger.info { "Analysis complete: ${findings.size} findings in ${elapsed}ms" }

        return AnalysisResult(
            findings = findings.sortedWith(compareBy({ it.location.file }, { it.location.line })),
            filesAnalyzed = irTrees.size,
            errors = errors,
            elapsedMs = elapsed,
        )
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

    private fun buildCallGraph(): CallGraph = CallGraph()

    private fun evaluateRules(
        irTrees: Map<String, FileRoot>,
        symbolTable: SymbolTable,
        callGraph: CallGraph,
    ): List<Finding> {
        val context = AnalysisContext(irTrees, symbolTable, callGraph, config)
        return rules
            .filter { rule -> config.ruleOverrides[rule.id]?.enabled != false }
            .flatMap { it.evaluate(context) }
    }

    private fun collectSymbols(
        nodes: List<IRNode>,
        symbolTable: SymbolTable,
    ) {
        for (node in nodes) {
            if (node is FunctionDecl) symbolTable.register(node)
            collectSymbols(node.children, symbolTable)
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
