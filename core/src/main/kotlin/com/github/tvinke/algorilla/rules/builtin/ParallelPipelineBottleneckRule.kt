package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.model.ExecutionContext
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LoopKind
import com.github.tvinke.algorilla.model.LoopNode
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Evidence
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.Rule
import com.github.tvinke.algorilla.rules.RuleCategory
import com.github.tvinke.algorilla.rules.Suggestion
import com.github.tvinke.algorilla.util.findDescendants

/**
 * Detects parallelStream().forEach() where the callback body mutates shared state.
 *
 * Using parallelStream() with shared mutable operations (add, put, remove, write, etc.)
 * negates parallelism benefits and risks thread-safety bugs. Common patterns:
 *
 * ```java
 * List<Result> results = new ArrayList<>();
 * items.parallelStream().forEach(item -> results.add(item.process()));
 * ```
 *
 * The fix is typically to use `.collect(Collectors.toList())` or a thread-safe accumulator.
 */
public class ParallelPipelineBottleneckRule : Rule {
    override val id: String = "parallel-pipeline-bottleneck"
    override val name: String = "Parallel Pipeline Bottleneck"
    override val severity: Severity = Severity.WARNING
    override val languages: Set<Language> = setOf(Language.JAVA, Language.KOTLIN, Language.GROOVY)
    override val category: RuleCategory = RuleCategory.CONCURRENCY
    override val aliases: List<String> = listOf("parallel-stream-bottleneck")

    override fun evaluate(context: AnalysisContext): List<Finding> {
        val findings = mutableListOf<Finding>()
        for ((_, fileRoot) in context.irTrees) {
            val mutationMethods = context.registry.mutationMethods(fileRoot.language)
            scanNode(fileRoot, mutationMethods, findings)
        }
        return findings
    }

    private fun scanNode(
        node: IRNode,
        mutationMethods: Set<String>,
        findings: MutableList<Finding>,
    ) {
        if (node is LoopNode && node.kind == LoopKind.PARALLEL_STREAM_FOR_EACH) {
            checkForSharedMutation(node, mutationMethods, findings)
            return
        }
        for (child in node.children) {
            scanNode(child, mutationMethods, findings)
        }
    }

    private fun checkForSharedMutation(
        parallelLoop: LoopNode,
        mutationMethods: Set<String>,
        findings: MutableList<Finding>,
    ) {
        val mutationCalls =
            parallelLoop
                .findDescendants<FunctionCall>()
                .filter { it.name in mutationMethods && it.qualifiedTarget != null }

        // The lambda parameter is typically the iterated variable — mutations on
        // something else are shared state mutations
        val iteratedVar = parallelLoop.iteratedVariable
        for (call in mutationCalls) {
            // If the mutation target is the iterated variable itself, it's fine
            // (e.g. item.add() where item is the lambda param — unlikely but safe)
            if (call.qualifiedTarget == iteratedVar) continue

            findings.add(buildFinding(parallelLoop, call))
        }
    }

    @Suppress("LongMethod") // Assembles concurrency-specific finding with thread-safety evidence
    private fun buildFinding(
        parallelLoop: LoopNode,
        mutationCall: FunctionCall,
    ): Finding {
        val target = mutationCall.qualifiedTarget ?: "shared"
        val iterVar = parallelLoop.iteratedVariable ?: "items"
        return Finding(
            ruleId = id,
            ruleName = name,
            severity = severity,
            location = mutationCall.location,
            message =
                "$target.${mutationCall.name}() inside " +
                    "parallelStream().forEach() mutates shared state",
            suggestions =
                listOf(
                    Suggestion.Freeform(
                        "Use .collect(Collectors.toList()) or a thread-safe " +
                            "accumulator instead of mutating shared state from parallel threads",
                    ),
                ),
            currentComplexity = "thread-unsafe",
            suggestedComplexity = "thread-safe",
            evidence =
                listOf(
                    Evidence(
                        parallelLoop.location,
                        "parallelStream().forEach() over $iterVar",
                        ExecutionContext.INSIDE_LOOP,
                        complexity = "parallel",
                    ),
                    Evidence(
                        mutationCall.location,
                        "$target.${mutationCall.name}() — shared state mutation",
                        ExecutionContext.INSIDE_LOOP,
                        depth = 1,
                        complexity = "bottleneck",
                    ),
                ),
        )
    }
}
