package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.model.ExecutionContext
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LookupCall
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.model.SortCall
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Evidence
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.Rule
import com.github.tvinke.algorilla.util.findDescendants

/**
 * Detects linear lookups inside sort comparators. When a comparator performs a
 * linear scan (e.g. list.contains()) on every comparison, the overall complexity
 * becomes O(n^2 log n) instead of O(n log n).
 */
public class ExpensiveSortComparatorRule : Rule {
    override val id: String = "expensive-sort-comparator"
    override val name: String = "Expensive Sort Comparator"
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
        if (node is SortCall) {
            checkComparatorBody(node, findings)
        }
        for (child in node.children) {
            scanNode(child, findings)
        }
    }

    private fun checkComparatorBody(
        sort: SortCall,
        findings: MutableList<Finding>,
    ) {
        val body = sort.comparatorBody ?: return
        val lookups = body.filterIsInstance<LookupCall>() + body.flatMap { it.findDescendants<LookupCall>() }
        for (lookup in lookups) {
            findings.add(buildFinding(sort, lookup))
        }
    }

    private fun buildFinding(
        sort: SortCall,
        lookup: LookupCall,
    ): Finding {
        val targetVar = lookup.targetVariable ?: "collection"
        val evidence =
            listOf(
                Evidence(
                    location = sort.location,
                    label = "${sort.kind.name.lowercase()} with comparator",
                    executionContext = ExecutionContext.SINGLE,
                ),
                Evidence(
                    location = lookup.location,
                    label = "linear ${lookup.kind.name.lowercase()} on '$targetVar' inside comparator",
                    executionContext = ExecutionContext.INSIDE_LOOP,
                ),
            )
        return Finding(
            ruleId = id,
            ruleName = name,
            severity = severity,
            location = sort.location,
            message = "Linear lookup on '$targetVar' inside sort comparator",
            suggestion = "Build a lookup Map from '$targetVar' before the sort",
            currentComplexity = "O(n^2 log n)",
            suggestedComplexity = "O(n log n)",
            evidence = evidence,
        )
    }
}
