package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.model.AccessKind
import com.github.tvinke.algorilla.model.CollectionAccess
import com.github.tvinke.algorilla.model.ExecutionContext
import com.github.tvinke.algorilla.model.FunctionCall
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
import com.github.tvinke.algorilla.util.CrossMethodResolver
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
            scanNode(fileRoot, context, findings)
        }
        return findings
    }

    private fun scanNode(
        node: IRNode,
        context: AnalysisContext,
        findings: MutableList<Finding>,
    ) {
        if (node is FunctionDecl) {
            checkFunction(node, context, findings)
        }
        for (child in node.children) {
            scanNode(child, context, findings)
        }
    }

    private fun checkFunction(
        fn: FunctionDecl,
        context: AnalysisContext,
        findings: MutableList<Finding>,
    ) {
        val sorts = fn.findDescendants<SortCall>()
        val accesses = fn.findDescendants<CollectionAccess>()
        val calls = fn.findDescendants<FunctionCall>()

        // Direct pairs within the same function
        val matchedSorts = mutableSetOf<SortCall>()
        val matchedAccesses = mutableSetOf<CollectionAccess>()
        for (sort in sorts) {
            for (access in accesses) {
                if (isNearby(sort, access)) {
                    findings.add(buildFinding(sort, access))
                    matchedSorts.add(sort)
                    matchedAccesses.add(access)
                }
            }
        }

        // Cross-method: sort here, access inside a called method
        for (sort in sorts.filter { it !in matchedSorts }) {
            checkSortWithCrossMethodAccess(sort, calls, context, findings)
        }

        // Cross-method: access here, sort inside a called method
        for (access in accesses.filter { it !in matchedAccesses }) {
            checkAccessWithCrossMethodSort(access, calls, context, findings)
        }
    }

    private fun checkSortWithCrossMethodAccess(
        sort: SortCall,
        calls: List<FunctionCall>,
        context: AnalysisContext,
        findings: MutableList<Finding>,
    ) {
        val nearbyCalls = calls.filter { isNearbyCall(sort, it) }
        val maxDepth = context.config.maxCallDepth.coerceAtMost(2)
        for (call in nearbyCalls) {
            val access =
                CrossMethodResolver.resolveAndFind<CollectionAccess>(
                    call,
                    context.symbolTable,
                    maxDepth = maxDepth,
                ) ?: continue
            findings.add(buildIndirectFinding(sort, call, access))
            return
        }
    }

    private fun checkAccessWithCrossMethodSort(
        access: CollectionAccess,
        calls: List<FunctionCall>,
        context: AnalysisContext,
        findings: MutableList<Finding>,
    ) {
        val nearbyCalls = calls.filter { isNearbyCallBefore(it, access) }
        val maxDepth = context.config.maxCallDepth.coerceAtMost(2)
        for (call in nearbyCalls) {
            val sort =
                CrossMethodResolver.resolveAndFind<SortCall>(
                    call,
                    context.symbolTable,
                    maxDepth = maxDepth,
                ) ?: continue
            findings.add(buildIndirectFinding(sort, call, access))
            return
        }
    }

    private fun isNearby(
        sort: SortCall,
        access: CollectionAccess,
    ): Boolean {
        val lineDiff = access.location.line - sort.location.line
        return lineDiff in 0..MAX_LINE_DISTANCE
    }

    private fun isNearbyCall(
        sort: SortCall,
        call: FunctionCall,
    ): Boolean {
        val lineDiff = call.location.line - sort.location.line
        return lineDiff in 0..MAX_CROSS_METHOD_LINE_DISTANCE
    }

    private fun isNearbyCallBefore(
        call: FunctionCall,
        access: CollectionAccess,
    ): Boolean {
        val lineDiff = access.location.line - call.location.line
        return lineDiff in 0..MAX_CROSS_METHOD_LINE_DISTANCE
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

    private fun buildIndirectFinding(
        sort: SortCall,
        call: FunctionCall,
        access: CollectionAccess,
    ): Finding {
        val cx = ComplexityModel.sortForAccess()
        return Finding(
            ruleId = id,
            ruleName = name,
            severity = severity,
            location = sort.location,
            message = "Sorting entire collection just to access ${accessLabel(access.kind)} element via ${call.name}()",
            suggestion = "Use .max(Comparator.comparing(...)) or .min(...) instead of sorting",
            currentComplexity = cx.current,
            suggestedComplexity = cx.suggested,
            evidence = buildIndirectEvidence(sort, call, access),
        )
    }

    private fun buildIndirectEvidence(
        sort: SortCall,
        call: FunctionCall,
        access: CollectionAccess,
    ): List<Evidence> =
        listOf(
            Evidence(
                sort.location,
                "${sort.kind.label} call",
                ExecutionContext.SINGLE,
                complexity = ComplexityModel.sortEvidence(isBottleneck = true),
            ),
            Evidence(call.location, "${call.name}() called after sort", ExecutionContext.SINGLE, depth = 1),
            Evidence(
                access.location,
                ".${access.kind.label} inside ${call.name}()",
                ExecutionContext.SINGLE,
                depth = 2,
                complexity = "O(1)",
            ),
        )

    private companion object {
        /** Max lines between sort and access for direct (same-expression-chain) matches. */
        const val MAX_LINE_DISTANCE = 2

        /** Slightly tighter for cross-method matches to reduce FPs on unrelated collections. */
        const val MAX_CROSS_METHOD_LINE_DISTANCE = 2
    }
}

private fun accessLabel(kind: AccessKind): String =
    when (kind) {
        AccessKind.FIRST, AccessKind.FIND_FIRST -> "the first"
        AccessKind.LAST, AccessKind.GET_SIZE_MINUS_1, AccessKind.POP -> "the last"
        AccessKind.FIND_ANY -> "a single"
        AccessKind.INDEX_ZERO -> "the first"
    }
