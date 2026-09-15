package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.model.Confidence
import com.github.tvinke.algorilla.model.ExecutionContext
import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LoopNode
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Evidence
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.Rule
import com.github.tvinke.algorilla.rules.RuleCategory
import com.github.tvinke.algorilla.rules.Suggestion
import com.github.tvinke.algorilla.util.walkLoopSites

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
    override val defaultConfidence: Confidence = Confidence.MEDIUM
    override val requiresTypeContext: Boolean = true

    override fun evaluate(context: AnalysisContext): List<Finding> {
        val findings = mutableListOf<Finding>()
        for ((_, fileRoot) in context.irTrees) {
            val language = (fileRoot as? FileRoot)?.language
            val methods = context.registry.copyOnModifyMethodsFor(language ?: Language.JAVA)
            fileRoot.walkLoopSites { node, _, loopStack ->
                if (node is FunctionCall && node.name in methods) {
                    findings.add(buildFinding(node, loopStack))
                }
            }
        }
        return findings
    }

    private fun buildFinding(
        call: FunctionCall,
        loopStack: List<LoopNode>,
    ): Finding {
        val outerLoop = loopStack.first()
        val loopVar = outerLoop.iteratedVariable ?: "items"
        val evidence =
            listOf(
                Evidence(outerLoop.location, outerLoop.kind.label(), ExecutionContext.INSIDE_LOOP, complexity = "O($loopVar)"),
                Evidence(
                    call.location,
                    "${call.name}() inside loop",
                    ExecutionContext.INSIDE_LOOP,
                    depth = 1,
                    complexity = "copy ← bottleneck",
                ),
            )
        return Finding(
            ruleId = id,
            ruleName = name,
            severity = severity,
            location = call.location,
            message = "${call.name}() inside ${outerLoop.kind.label()} repeatedly copies collection",
            suggestions = listOf(Suggestion.Freeform("Accumulate into a single collection, then call ${call.name}() once after the loop")),
            currentComplexity = "O($loopVar * copy)",
            suggestedComplexity = "O($loopVar + copy)",
            evidence = evidence,
        )
    }
}
