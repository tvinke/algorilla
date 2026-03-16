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
import com.github.tvinke.algorilla.rules.Suggestion
import com.github.tvinke.algorilla.semantics.SemanticCategory

/**
 * Detects expensive serialization/deserialization calls inside loops.
 * Operations like writeValueAsString, readValue, JSON.stringify are
 * often O(n) themselves, making the loop O(n^2) overall.
 */
public class ExpensiveSerializationInLoopRule : Rule {
    override val id: String = "expensive-serialization-in-loop"
    override val name: String = "Expensive Serialization In Loop"
    override val severity: Severity = Severity.WARNING
    override val languages: Set<Language> = Language.entries.toSet()
    override val category: RuleCategory = RuleCategory.LOOP_AMPLIFIER

    override fun evaluate(context: AnalysisContext): List<Finding> {
        val findings = mutableListOf<Finding>()
        for ((_, fileRoot) in context.irTrees) {
            val lang = fileRoot.language
            scanNode(fileRoot, emptyList(), lang, context, findings)
        }
        return findings
    }

    private fun scanNode(
        node: IRNode,
        loopStack: List<LoopNode>,
        language: Language,
        context: AnalysisContext,
        findings: MutableList<Finding>,
    ) {
        if (node is LoopNode) {
            for (child in node.children) {
                scanNode(child, loopStack + node, language, context, findings)
            }
            return
        }

        if (loopStack.isNotEmpty() && node is FunctionCall) {
            val semantics = context.registry.classify(language, node.name)
            if (semantics?.category == SemanticCategory.SERIALIZATION) {
                findings.add(buildFinding(node, loopStack))
            }
        }

        for (child in node.children) {
            scanNode(child, loopStack, language, context, findings)
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
                    complexity = "serialize \u2190 bottleneck",
                ),
            )
        return Finding(
            ruleId = id,
            ruleName = name,
            severity = severity,
            location = call.location,
            message = "Serialization call ${call.name}() inside ${outerLoop.kind.label()}",
            suggestions = listOf(Suggestion.Freeform("Move serialization outside the loop, or use batch serialization")),
            currentComplexity = "O(|$loopVar| * serialize)",
            suggestedComplexity = "O(|$loopVar|)",
            evidence = evidence,
        )
    }
}
