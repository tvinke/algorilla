package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.model.ExecutionContext
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LookupCall
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Evidence
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.Rule
import com.github.tvinke.algorilla.rules.RuleCategory
import com.github.tvinke.algorilla.util.findDescendants
import com.github.tvinke.algorilla.util.hasO1Type

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
        val lookups = fn.findDescendants<LookupCall>()
        val grouped =
            lookups
                .filter { it.targetVariable != null && !it.isO1 && !fn.hasO1Type(it.targetVariable) }
                .groupBy { it.targetVariable }
        for ((targetVar, calls) in grouped) {
            if (calls.size >= MIN_LOOKUPS_TO_REPORT) {
                findings.add(buildFinding(fn, targetVar!!, calls))
            }
        }
    }

    private fun buildFinding(
        fn: FunctionDecl,
        targetVar: String,
        lookups: List<LookupCall>,
    ): Finding {
        val evidence =
            lookups.map { lookup ->
                Evidence(
                    location = lookup.location,
                    label = "${lookup.kind.name.lowercase()} on '$targetVar'",
                    executionContext = ExecutionContext.SINGLE,
                )
            }
        return Finding(
            ruleId = id,
            ruleName = name,
            severity = severity,
            location = fn.location,
            message = "${lookups.size} linear scans on '$targetVar' in ${fn.name}()",
            suggestion = "Cache the result of the first lookup, or combine into a single pass",
            currentComplexity = "O(n*k)",
            suggestedComplexity = "O(n)",
            evidence = evidence,
        )
    }

    internal companion object {
        const val MIN_LOOKUPS_TO_REPORT = 2
    }
}
