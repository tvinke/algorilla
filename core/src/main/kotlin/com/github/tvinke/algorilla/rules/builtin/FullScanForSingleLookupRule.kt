package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.model.CollectionAccess
import com.github.tvinke.algorilla.model.ExecutionContext
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LookupCall
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.ComplexityModel
import com.github.tvinke.algorilla.rules.Evidence
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.Rule
import com.github.tvinke.algorilla.rules.RuleCategory
import com.github.tvinke.algorilla.semantics.LanguageSemanticsRegistry
import com.github.tvinke.algorilla.util.findDescendants

/**
 * Detects loading all records from a data source then filtering for one in memory.
 * A targeted query is typically O(1) compared to loading everything at O(n).
 */
public class FullScanForSingleLookupRule : Rule {
    override val id: String = "bulk-load-for-single-lookup"
    override val name: String = "Bulk Load For Single Lookup"
    override val aliases: List<String> = listOf("full-scan-for-single-lookup")
    override val severity: Severity = Severity.WARNING
    override val languages: Set<Language> = Language.entries.toSet()
    override val category: RuleCategory = RuleCategory.QUERY_PATTERN

    override fun evaluate(context: AnalysisContext): List<Finding> {
        val findings = mutableListOf<Finding>()
        for ((_, fileRoot) in context.irTrees) {
            scanNode(fileRoot, findings)
        }
        return findings
    }

    private fun scanNode(
        node: IRNode,
        findings: MutableList<Finding>,
    ) {
        if (node is FunctionDecl) {
            checkFunction(node, findings)
        }
        for (child in node.children) {
            scanNode(child, findings)
        }
    }

    private fun checkFunction(
        fn: FunctionDecl,
        findings: MutableList<Finding>,
    ) {
        val bulkCalls = fn.findDescendants<FunctionCall>().filter { isBulkLoadCall(it) }
        if (bulkCalls.isEmpty()) return
        val hasFilter =
            fn.findDescendants<LookupCall>().isNotEmpty() ||
                fn.findDescendants<CollectionAccess>().isNotEmpty()
        if (!hasFilter) return
        for (call in bulkCalls) {
            findings.add(buildFinding(fn, call))
        }
    }

    private fun buildFinding(
        fn: FunctionDecl,
        call: FunctionCall,
    ): Finding {
        val cx = ComplexityModel.fullScanForLookup()
        val evidence =
            listOf(
                Evidence(
                    location = call.location,
                    label = "${call.name}() loads all records",
                    executionContext = ExecutionContext.SINGLE,
                    complexity = ComplexityModel.bottleneck("O(n)"),
                ),
                Evidence(
                    location = fn.location,
                    label = "followed by in-memory filtering in ${fn.name}()",
                    executionContext = ExecutionContext.SINGLE,
                    complexity = "O(n)",
                ),
            )
        return Finding(
            ruleId = id,
            ruleName = name,
            severity = severity,
            location = call.location,
            message = "${call.name}() followed by in-memory filtering in ${fn.name}()",
            suggestion = "Use a targeted query instead of loading all records and filtering in memory",
            currentComplexity = cx.current,
            suggestedComplexity = cx.suggested,
            evidence = evidence,
        )
    }
}

internal fun isBulkLoadCall(call: FunctionCall): Boolean {
    if (!BULK_LOAD_PREFIXES.any { call.name.startsWith(it, ignoreCase = true) }) return false
    // Exclude DOM/test framework targets (e.g., wrapper.findAll in Vue test utils)
    val target = call.qualifiedTarget?.lowercase()
    if (target != null && DOM_TARGETS.any { target.contains(it) }) return false
    return true
}

private val BULK_LOAD_PREFIXES: List<String> by lazy {
    LanguageSemanticsRegistry.loadDefaults().allBulkLoadPrefixes()
}

private val DOM_TARGETS = setOf("wrapper", "document", "element", "el", "node", "dom", "selector")
