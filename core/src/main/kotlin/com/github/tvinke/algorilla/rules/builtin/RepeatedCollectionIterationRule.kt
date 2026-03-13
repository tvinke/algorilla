package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.model.Confidence
import com.github.tvinke.algorilla.model.ExecutionContext
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LoopKind
import com.github.tvinke.algorilla.model.LoopNode
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.ComplexityModel
import com.github.tvinke.algorilla.rules.Evidence
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.Rule
import com.github.tvinke.algorilla.rules.RuleCategory
import com.github.tvinke.algorilla.util.findDescendantsWithBranchContext
import com.github.tvinke.algorilla.util.maxCoExecutableSubset

/**
 * Detects repeated iteration over the same collection within a single function that could
 * potentially be combined into a single pass. Covers two patterns:
 *
 * - **Stream pipelines:** two `.stream()` calls on the same collection variable.
 * - **For-each loops:** two for-each loops iterating the same variable.
 *
 * These are flagged as informational suggestions — multiple passes are often intentional
 * for readability, but worth calling out for hot paths.
 */
public class RepeatedCollectionIterationRule : Rule {
    override val id: String = "repeated-collection-iteration"
    override val name: String = "Repeated Collection Iteration"
    override val severity: Severity = Severity.INFO
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
        val reportedTargets = checkStreamPipelines(fn, findings)
        checkForEachLoops(fn, reportedTargets, findings)
    }

    /**
     * Pattern A: two `.stream()` calls on the same qualifiedTarget within the same function,
     * in compatible branch contexts.
     */
    private fun checkStreamPipelines(
        fn: FunctionDecl,
        findings: MutableList<Finding>,
    ): Set<String?> {
        val streamsWithContext = fn.findDescendantsWithBranchContext<FunctionCall>()
        val filtered =
            streamsWithContext.filter {
                it.first.name == "stream" && it.first.qualifiedTarget != null
            }
        val byTarget = filtered.groupBy { it.first.qualifiedTarget }
        val reported = mutableSetOf<String?>()

        for ((target, callsWithContext) in byTarget) {
            val coExecutable = maxCoExecutableSubset(callsWithContext)
            if (coExecutable.size >= MIN_PASSES_TO_REPORT) {
                findings.add(buildStreamFinding(fn, target!!, coExecutable))
                reported.add(target)
            }
        }
        return reported
    }

    /**
     * Pattern B: two for-each loops over the same iteratedVariable within the same function,
     * in compatible branch contexts. Skips targets already reported by the stream check.
     */
    private fun checkForEachLoops(
        fn: FunctionDecl,
        reportedTargets: Set<String?>,
        findings: MutableList<Finding>,
    ) {
        val loopsWithContext = fn.findDescendantsWithBranchContext<LoopNode>()
        val filtered =
            loopsWithContext.filter {
                it.first.kind == LoopKind.FOR_EACH &&
                    it.first.iteratedVariable != null &&
                    it.first.iteratedVariable !in reportedTargets
            }
        val byTarget = filtered.groupBy { it.first.iteratedVariable }

        for ((target, callsWithContext) in byTarget) {
            val coExecutable = maxCoExecutableSubset(callsWithContext)
            if (coExecutable.size >= MIN_PASSES_TO_REPORT) {
                findings.add(buildForEachFinding(fn, target!!, coExecutable))
            }
        }
    }

    private fun buildStreamFinding(
        fn: FunctionDecl,
        target: String,
        calls: List<FunctionCall>,
    ): Finding {
        val cx = ComplexityModel.repeatedScans(calls.size)
        return Finding(
            ruleId = id,
            ruleName = name,
            severity = severity,
            location = calls.first().location,
            message =
                "'$target' is streamed ${calls.size} times in ${fn.name}() — " +
                    "consider fusing into a single pipeline",
            suggestion =
                "Combine the stream operations on '$target' into a single pass " +
                    "using reduce, Collectors.teeing, or a custom collector",
            confidence = Confidence.MEDIUM,
            currentComplexity = cx.current,
            suggestedComplexity = cx.suggested,
            evidence = buildStreamEvidence(calls, target),
        )
    }

    private fun buildStreamEvidence(
        calls: List<FunctionCall>,
        target: String,
    ): List<Evidence> =
        calls.mapIndexed { idx, call ->
            val tag = if (idx == 0) "1st" else "#${idx + 1}"
            Evidence(
                location = call.location,
                label = ".stream() on '$target' ($tag)",
                executionContext = ExecutionContext.SINGLE,
                complexity = ComplexityModel.loopEvidence(target),
            )
        }

    private fun buildForEachFinding(
        fn: FunctionDecl,
        target: String,
        loops: List<LoopNode>,
    ): Finding {
        val cx = ComplexityModel.repeatedScans(loops.size)
        return Finding(
            ruleId = id,
            ruleName = name,
            severity = severity,
            location = loops.first().location,
            message =
                "'$target' is iterated ${loops.size} times in ${fn.name}() — " +
                    "the loops could be merged into a single pass",
            suggestion =
                "Merge the for-each loops over '$target' into one loop that " +
                    "performs all operations per element",
            confidence = Confidence.LOW,
            currentComplexity = cx.current,
            suggestedComplexity = cx.suggested,
            evidence = buildForEachEvidence(loops, target),
        )
    }

    private fun buildForEachEvidence(
        loops: List<LoopNode>,
        target: String,
    ): List<Evidence> =
        loops.mapIndexed { idx, loop ->
            val tag = if (idx == 0) "1st" else "#${idx + 1}"
            Evidence(
                location = loop.location,
                label = "for-each over '$target' ($tag)",
                executionContext = ExecutionContext.SINGLE,
                complexity = ComplexityModel.loopEvidence(target),
            )
        }

    internal companion object {
        const val MIN_PASSES_TO_REPORT = 2
    }
}
