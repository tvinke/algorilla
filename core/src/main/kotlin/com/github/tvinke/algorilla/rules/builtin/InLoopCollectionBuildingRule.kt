package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.model.ExecutionContext
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LoopNode
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Evidence
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.Rule
import com.github.tvinke.algorilla.rules.RuleCategory

/**
 * Detects patterns that repeatedly copy or rebuild collections inside loops:
 * - addAll() inside a loop (copies entire collection each time)
 * - concat() (JS) inside a loop (creates new array each time)
 *
 * These turn O(n) loops into O(n^2) due to repeated copying.
 */
public class InLoopCollectionBuildingRule : Rule {
    override val id: String = "in-loop-collection-building"
    override val name: String = "In Loop Collection Building"
    override val severity: Severity = Severity.WARNING
    override val languages: Set<Language> = Language.entries.toSet()
    override val category: RuleCategory = RuleCategory.LOOP_AMPLIFIER

    override fun evaluate(context: AnalysisContext): List<Finding> {
        val findings = mutableListOf<Finding>()
        for ((_, fileRoot) in context.irTrees) {
            scanNode(fileRoot, emptyList(), findings)
        }
        return findings
    }

    private fun scanNode(
        node: IRNode,
        loopStack: List<LoopNode>,
        findings: MutableList<Finding>,
    ) {
        if (node is LoopNode) {
            for (child in node.children) {
                scanNode(child, loopStack + node, findings)
            }
            return
        }

        if (loopStack.isNotEmpty() && node is FunctionCall && isCopyOnModifyCall(node.name)) {
            findings.add(buildFinding(node, loopStack))
        }

        for (child in node.children) {
            scanNode(child, loopStack, findings)
        }
    }

    private fun buildFinding(
        call: FunctionCall,
        loopStack: List<LoopNode>,
    ): Finding {
        val outerLoop = loopStack.first()
        val loopVar = outerLoop.iteratedVariable ?: "items"
        val evidence =
            listOf(
                Evidence(outerLoop.location, outerLoop.kind.label(), ExecutionContext.INSIDE_LOOP, complexity = "O(|$loopVar|)"),
                Evidence(
                    call.location,
                    "${call.name}() inside loop",
                    ExecutionContext.INSIDE_LOOP,
                    depth = 1,
                    complexity = "copy \u2190 bottleneck",
                ),
            )
        return Finding(
            ruleId = id,
            ruleName = name,
            severity = severity,
            location = call.location,
            message = "${call.name}() inside ${outerLoop.kind.label()} repeatedly copies collection",
            suggestion = "Accumulate into a single collection, then call ${call.name}() once after the loop",
            currentComplexity = "O(|$loopVar| * copy)",
            suggestedComplexity = "O(|$loopVar| + copy)",
            evidence = evidence,
        )
    }
}

private val COPY_ON_MODIFY_METHODS = setOf("addAll", "concat", "putAll")

private fun isCopyOnModifyCall(name: String): Boolean = name in COPY_ON_MODIFY_METHODS
