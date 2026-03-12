package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.model.ExecutionContext
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
import com.github.tvinke.algorilla.util.findDescendantsWithBranchContext
import com.github.tvinke.algorilla.util.isCollectionLookup
import com.github.tvinke.algorilla.util.maxCoExecutableSubset

/**
 * Detects multiple linear scans on the same collection within a single function.
 * Each additional scan multiplies the cost; caching or combining into a single pass is preferred.
 */
public class RepeatedLinearScanRule : Rule {
    override val id: String = "repeated-linear-scan"
    override val name: String = "Repeated Linear Scan"
    override val severity: Severity = Severity.WARNING
    override val languages: Set<Language> = Language.entries.toSet()
    override val category: RuleCategory = RuleCategory.REDUNDANCY

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
        val lookupsWithContext = fn.findDescendantsWithBranchContext<LookupCall>()
        val filtered =
            lookupsWithContext.filter {
                it.first.targetVariable != null &&
                    it.first.isCollectionLookup(fn)
            }
        val grouped = filtered.groupBy { it.first.targetVariable }
        for ((targetVar, callsWithContext) in grouped) {
            val coExecutable = maxCoExecutableSubset(callsWithContext)
            if (coExecutable.size >= MIN_LOOKUPS_TO_REPORT) {
                findings.add(buildFinding(fn, targetVar!!, coExecutable))
            }
        }
    }

    private fun buildFinding(
        fn: FunctionDecl,
        targetVar: String,
        lookups: List<LookupCall>,
    ): Finding {
        val cx = ComplexityModel.repeatedScans(lookups.size)
        val ops = lookups.map { ".${it.kind.label}()" }
        val evidence =
            lookups.mapIndexed { idx, lookup ->
                val tag = if (idx == 0) "1st" else "#${idx + 1}"
                Evidence(
                    location = lookup.location,
                    label = ".${lookup.kind.label}() on '$targetVar' ($tag)",
                    executionContext = ExecutionContext.SINGLE,
                    complexity = ComplexityModel.loopEvidence(targetVar),
                )
            }
        val opsDesc = ops.joinToString(" and ")
        return Finding(
            ruleId = id,
            ruleName = name,
            severity = severity,
            location = lookups.first().location,
            message =
                "'$targetVar' is scanned ${lookups.size} times in ${fn.name}(): " +
                    "each $opsDesc iterates the full collection",
            suggestion =
                "Combine into a single pass, or convert '$targetVar' to a Set/Map for O(1) lookups",
            currentComplexity = cx.current,
            suggestedComplexity = cx.suggested,
            evidence = evidence,
        )
    }

    internal companion object {
        const val MIN_LOOKUPS_TO_REPORT = 2
    }
}
