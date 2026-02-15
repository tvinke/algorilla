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
import com.github.tvinke.algorilla.rules.Evidence
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.Rule
import com.github.tvinke.algorilla.util.findDescendants

/**
 * Detects loading all records from a data source then filtering for one in memory.
 * A targeted query is typically O(1) compared to loading everything at O(n).
 */
public class FullScanForSingleLookupRule : Rule {
    override val id: String = "full-scan-for-single-lookup"
    override val name: String = "Full Scan For Single Lookup"
    override val severity: Severity = Severity.WARNING
    override val languages: Set<Language> = Language.entries.toSet()

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
        val bulkCalls = fn.findDescendants<FunctionCall>().filter { isBulkLoadCall(it.name) }
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
        val evidence =
            listOf(
                Evidence(
                    location = call.location,
                    label = "${call.name}() loads all records",
                    executionContext = ExecutionContext.SINGLE,
                ),
                Evidence(
                    location = fn.location,
                    label = "followed by in-memory filtering in ${fn.name}()",
                    executionContext = ExecutionContext.SINGLE,
                ),
            )
        return Finding(
            ruleId = id,
            ruleName = name,
            severity = severity,
            location = call.location,
            message = "${call.name}() followed by in-memory filtering in ${fn.name}()",
            suggestion = "Use a targeted query instead of loading all records and filtering in memory",
            currentComplexity = "O(n)",
            suggestedComplexity = "O(1)",
            evidence = evidence,
        )
    }
}

internal fun isBulkLoadCall(name: String): Boolean = BULK_LOAD_PREFIXES.any { name.startsWith(it, ignoreCase = true) }

private val BULK_LOAD_PREFIXES: List<String> =
    listOf(
        "findAll",
        "getAll",
        "listAll",
        "loadAll",
        "fetchAll",
    )
