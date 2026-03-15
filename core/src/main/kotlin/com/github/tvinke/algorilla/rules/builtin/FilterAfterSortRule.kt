package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.model.Confidence
import com.github.tvinke.algorilla.model.ExecutionContext
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LookupCall
import com.github.tvinke.algorilla.model.LookupKind
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.model.SortCall
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.ComplexityModel
import com.github.tvinke.algorilla.rules.Evidence
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.Rule
import com.github.tvinke.algorilla.rules.RuleCategory
import com.github.tvinke.algorilla.rules.Suggestion

/**
 * Detects stream/collection pipelines where filter() comes after sort().
 * Sorting first processes all N elements at O(n log n), then filter discards some.
 * Filtering first reduces the input to k elements, then sorting is O(k log k) where k ≤ n.
 *
 * Example:
 * ```
 * items.stream().sorted(comparator).filter(predicate).collect(toList())
 * ```
 * Should be reordered to:
 * ```
 * items.stream().filter(predicate).sorted(comparator).collect(toList())
 * ```
 */
public class FilterAfterSortRule : Rule {
    override val id: String = "filter-after-sort"
    override val category: RuleCategory = RuleCategory.SORT_ABUSE
    override val defaultConfidence: Confidence = Confidence.HIGH
    override val name: String = "Filter After Sort"
    override val severity: Severity = Severity.INFO
    override val languages: Set<Language> = Language.entries.toSet()

    override fun evaluate(context: AnalysisContext): List<Finding> {
        val findings = mutableListOf<Finding>()
        for ((_, fileRoot) in context.irTrees) {
            scanNode(fileRoot, findings)
        }
        return findings
    }

    /**
     * Recursively scan the IR tree. At each node, check if its direct children
     * contain a sort-then-filter sequence (same chain = same parent's children list).
     */
    private fun scanNode(
        node: IRNode,
        findings: MutableList<Finding>,
    ) {
        checkSiblings(node.children, findings)
        for (child in node.children) {
            scanNode(child, findings)
        }
    }

    /**
     * Within a single list of sibling nodes, look for a SortCall followed by
     * a LookupCall(FILTER). Because the parser flattens chain calls into sibling
     * order (left-to-right), siblings in the same list are part of the same
     * expression/pipeline.
     */
    private fun checkSiblings(
        siblings: List<IRNode>,
        findings: MutableList<Finding>,
    ) {
        var lastSort: SortCall? = null
        for (node in siblings) {
            if (node is SortCall) {
                lastSort = node
            } else if (node is LookupCall && node.kind == LookupKind.FILTER && lastSort != null) {
                findings.add(buildFinding(lastSort, node))
                lastSort = null
            }
        }
    }

    private fun buildEvidence(
        sort: SortCall,
        filter: LookupCall,
    ): List<Evidence> =
        listOf(
            Evidence(
                sort.location,
                "${sort.kind.label} call \u2014 sorts all N elements",
                ExecutionContext.SINGLE,
                complexity = ComplexityModel.sortEvidence(isBottleneck = true),
            ),
            Evidence(
                filter.location,
                "filter after sort \u2014 discards elements after sorting",
                ExecutionContext.SINGLE,
                depth = 1,
                complexity = "O(n)",
            ),
        )

    private fun buildFinding(
        sort: SortCall,
        filter: LookupCall,
    ): Finding {
        val cx = ComplexityModel.filterAfterSort()
        return Finding(
            ruleId = id,
            ruleName = name,
            severity = severity,
            location = filter.location,
            message = "filter() after ${sort.kind.label}() — sorting all elements before filtering",
            suggestions =
                listOf(
                    Suggestion.ReorderOperations(
                        first = "filter()",
                        second = "${sort.kind.label}()",
                        reason = "sort fewer elements",
                    ),
                ),
            currentComplexity = cx.current,
            suggestedComplexity = cx.suggested,
            evidence = buildEvidence(sort, filter),
        )
    }
}
