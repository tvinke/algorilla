package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.model.CollectionAccess
import com.github.tvinke.algorilla.model.ExecutionContext
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.model.SortCall
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Evidence
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.Rule
import com.github.tvinke.algorilla.util.findDescendants

/**
 * Detects sorting an entire collection just to retrieve the first or last element.
 * Using `.max()` or `.min()` is O(n), while sorting first is O(n log n).
 */
public class SortForLastRule : Rule {
    override val id: String = "sort-for-last"
    override val name: String = "Sort For Last"
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
            val accessNodes = node.findDescendants<CollectionAccess>()
            for (access in accessNodes) {
                findings.add(buildFinding(node, access))
            }
        }
        for (child in node.children) {
            scanNode(child, findings)
        }
    }

    private fun buildFinding(
        sort: SortCall,
        access: CollectionAccess,
    ): Finding {
        val evidence =
            listOf(
                Evidence(
                    location = sort.location,
                    label = "${sort.kind.name.lowercase()} call",
                    executionContext = ExecutionContext.SINGLE,
                ),
                Evidence(
                    location = access.location,
                    label = ".${access.kind.name.lowercase()} after sort",
                    executionContext = ExecutionContext.SINGLE,
                ),
            )
        return Finding(
            ruleId = id,
            ruleName = name,
            severity = severity,
            location = sort.location,
            message = "Sorting entire collection just to access ${access.kind.name.lowercase()} element",
            suggestion = "Use .max(Comparator.comparing(...)) or .min(...) instead of sorting",
            currentComplexity = "O(n log n)",
            suggestedComplexity = "O(n)",
            evidence = evidence,
        )
    }
}
