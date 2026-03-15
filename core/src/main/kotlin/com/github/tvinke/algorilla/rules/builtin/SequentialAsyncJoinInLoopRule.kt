package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.model.Confidence
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
import com.github.tvinke.algorilla.semantics.LanguageSemanticsRegistry
import com.github.tvinke.algorilla.semantics.SemanticCategory

/**
 * Detects blocking calls (.join(), .get()) on futures inside loops.
 * Awaiting each future sequentially negates the benefit of async execution.
 * Collect futures first, then await them all at once.
 */
public class SequentialAsyncJoinInLoopRule : Rule {
    override val id: String = "sequential-async-join-in-loop"
    override val name: String = "Sequential Async Join In Loop"
    override val severity: Severity = Severity.WARNING
    override val languages: Set<Language> = Language.entries.toSet()
    override val category: RuleCategory = RuleCategory.LOOP_AMPLIFIER
    override val defaultConfidence: Confidence = Confidence.HIGH

    override fun evaluate(context: AnalysisContext): List<Finding> {
        val findings = mutableListOf<Finding>()
        for ((_, fileRoot) in context.irTrees) {
            scanNode(fileRoot, emptyList(), fileRoot.language, context, findings)
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
            if (semantics?.category == SemanticCategory.BLOCKING && looksLikeFutureCall(node, language, context.registry)) {
                findings.add(buildFinding(node, loopStack))
            }
        }

        for (child in node.children) {
            scanNode(child, loopStack, language, context, findings)
        }
    }

    @Suppress("LongMethod")
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
                    ".${call.name}() blocks on each iteration",
                    ExecutionContext.INSIDE_LOOP,
                    depth = 1,
                    complexity = "wait \u2190 bottleneck",
                ),
            )
        return Finding(
            ruleId = id,
            ruleName = name,
            severity = severity,
            location = call.location,
            message = "Blocking .${call.name}() on future inside ${outerLoop.kind.label()}",
            suggestions =
                listOf(
                    Suggestion.Freeform(
                        "Collect all futures first, then call .join()/.get() outside the loop (e.g. CompletableFuture.allOf)",
                    ),
                ),
            currentComplexity = "O(|$loopVar| * wait)",
            suggestedComplexity = "O(max-wait)",
            evidence = evidence,
        )
    }
}

/**
 * Heuristic: the call target variable name hints at a Future type.
 */
private fun looksLikeFutureCall(
    call: FunctionCall,
    language: Language,
    registry: LanguageSemanticsRegistry,
): Boolean {
    val target = call.qualifiedTarget?.lowercase() ?: return false
    return registry.futureIndicators(language).any { target.contains(it) }
}
