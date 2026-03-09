package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.model.AccessKind
import com.github.tvinke.algorilla.model.CollectionAccess
import com.github.tvinke.algorilla.model.ExecutionContext
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.model.SortCall
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.ComplexityModel
import com.github.tvinke.algorilla.rules.Evidence
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.Rule
import com.github.tvinke.algorilla.rules.RuleCategory
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
    override val category: RuleCategory = RuleCategory.SORT_ABUSE

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
        val sorts = fn.findDescendants<SortCall>()
        val accesses = fn.findDescendants<CollectionAccess>()
        if (sorts.isEmpty() || accesses.isEmpty()) return

        for (sort in sorts) {
            for (access in accesses) {
                if (isNearby(sort, access)) {
                    findings.add(buildFinding(sort, access))
                }
            }
        }
    }

    private fun isNearby(
        sort: SortCall,
        access: CollectionAccess,
    ): Boolean {
        val lineDiff = access.location.line - sort.location.line
        return lineDiff in 0..MAX_LINE_DISTANCE
    }

    private fun buildEvidence(
        sort: SortCall,
        access: CollectionAccess,
    ): List<Evidence> =
        listOf(
            Evidence(
                sort.location,
                "${sort.kind.label} call",
                ExecutionContext.SINGLE,
                complexity = ComplexityModel.sortEvidence(isBottleneck = true),
            ),
            Evidence(access.location, ".${access.kind.label} after sort", ExecutionContext.SINGLE, depth = 1, complexity = "O(1)"),
        )

    private fun buildFinding(
        sort: SortCall,
        access: CollectionAccess,
    ): Finding {
        val cx = ComplexityModel.sortForAccess()
        return Finding(
            ruleId = id,
            ruleName = name,
            severity = severity,
            location = sort.location,
            message = "Sorting entire collection just to access ${accessLabel(access.kind)} element",
            suggestion = "Use .max(Comparator.comparing(...)) or .min(...) instead of sorting",
            currentComplexity = cx.current,
            suggestedComplexity = cx.suggested,
            evidence = buildEvidence(sort, access),
        )
    }

    private companion object {
        const val MAX_LINE_DISTANCE = 5
    }
}

private fun accessLabel(kind: AccessKind): String =
    when (kind) {
        AccessKind.FIRST, AccessKind.FIND_FIRST -> "the first"
        AccessKind.LAST, AccessKind.GET_SIZE_MINUS_1, AccessKind.POP -> "the last"
        AccessKind.FIND_ANY -> "a single"
        AccessKind.INDEX_ZERO -> "the first"
    }
