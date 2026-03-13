package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.model.Confidence
import com.github.tvinke.algorilla.model.ExecutionContext
import com.github.tvinke.algorilla.model.FlowTarget
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LoopNode
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Evidence
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.Rule
import com.github.tvinke.algorilla.rules.RuleCategory
import com.github.tvinke.algorilla.semantics.LanguageSemanticsRegistry
import com.github.tvinke.algorilla.util.ParameterFlowQuery

/**
 * Detects IO operations (HTTP calls, database queries, file operations) inside loops.
 * Each iteration incurs network/disk latency, making the loop O(n * IO) in wall-clock time.
 *
 * Uses a two-tier classification:
 * - `io-methods`: unambiguous IO method names (executeQuery, getForObject) — always flagged
 * - `io-method-candidates`: ambiguous names (save, delete, put, write) — only flagged
 *   when the target variable matches an IO-capable pattern (repository, service, entityManager)
 *
 * Both tiers are loaded from YAML per language and framework overlay.
 */
public class IOInLoopRule : Rule {
    override val id: String = "io-in-loop"
    override val name: String = "IO In Loop"
    override val severity: Severity = Severity.WARNING
    override val languages: Set<Language> = Language.entries.toSet()
    override val category: RuleCategory = RuleCategory.LOOP_AMPLIFIER

    override fun evaluate(context: AnalysisContext): List<Finding> {
        val findings = mutableListOf<Finding>()
        for ((_, fileRoot) in context.irTrees) {
            val language = fileRoot.language
            val ioMethods = context.registry.ioMethods(language)
            val ioCandidates = context.registry.ioMethodCandidates(language)
            if (ioMethods.isEmpty() && ioCandidates.isEmpty()) continue
            scanNode(fileRoot, null, emptyList(), ioMethods, ioCandidates, language, context, findings)
        }
        return findings
    }

    @Suppress("LongParameterList")
    private fun scanNode(
        node: IRNode,
        enclosingFn: FunctionDecl?,
        loopStack: List<LoopNode>,
        ioMethods: Set<String>,
        ioCandidates: Set<String>,
        language: Language,
        context: AnalysisContext,
        findings: MutableList<Finding>,
    ) {
        val fn = if (node is FunctionDecl) node else enclosingFn

        if (node is LoopNode) {
            for (child in node.children) {
                scanNode(child, fn, loopStack + node, ioMethods, ioCandidates, language, context, findings)
            }
            return
        }

        if (loopStack.isNotEmpty() && node is FunctionCall) {
            val isDefiniteIO = node.name in ioMethods && !isInMemoryTarget(node, language, context.registry)
            val isCandidateIO = !isDefiniteIO && node.name in ioCandidates && isIOTarget(node, language, context.registry)

            if (isDefiniteIO || isCandidateIO) {
                val loopParamConfirmed =
                    fn != null &&
                        fn.parameterFlows.any { flow ->
                            flow.flowsInto.any { it is FlowTarget.LoopIteration }
                        }
                findings.add(buildFinding(node, loopStack, loopParamConfirmed))
            } else if (fn != null) {
                checkCrossMethodIO(node, fn, loopStack, context, findings)
            }
        }

        for (child in node.children) {
            scanNode(child, fn, loopStack, ioMethods, ioCandidates, language, context, findings)
        }
    }

    private fun checkCrossMethodIO(
        call: FunctionCall,
        callerFn: FunctionDecl,
        loopStack: List<LoopNode>,
        context: AnalysisContext,
        findings: MutableList<Finding>,
    ) {
        val evidence =
            ParameterFlowQuery.parameterFlowsThrough(
                call = call,
                callerFn = callerFn,
                symbolTable = context.symbolTable,
                maxDepth = context.config.maxCallDepth.coerceAtMost(2),
            ) { target ->
                // Check if the terminal operation is a method call to an IO method
                target is FlowTarget.MethodCallReceiver &&
                    context.registry.allIoMethods().contains(target.methodName)
            }
        if (evidence != null) {
            findings.add(buildCrossMethodFinding(call, evidence.paramName, loopStack))
        }
    }

    private fun buildFinding(
        call: FunctionCall,
        loopStack: List<LoopNode>,
        flowConfirmed: Boolean = false,
    ): Finding {
        val outerLoop = loopStack.first()
        val loopVar = outerLoop.iteratedVariable ?: "items"
        return Finding(
            ruleId = id,
            ruleName = name,
            severity = severity,
            confidence = if (flowConfirmed) Confidence.HIGH else Confidence.MEDIUM,
            location = call.location,
            message =
                "IO call ${call.name}() inside ${outerLoop.kind.label()} — " +
                    "each iteration incurs network/disk latency",
            suggestion =
                "Batch the IO operation outside the loop, " +
                    "or use a bulk API (e.g. saveAll, findAllById)",
            currentComplexity = "O(|$loopVar| \u00d7 IO)",
            suggestedComplexity = "O(1) IO + O(|$loopVar|)",
            evidence = buildEvidence(call, outerLoop, loopVar),
        )
    }

    @Suppress("LongMethod") // Straightforward Finding construction with evidence
    private fun buildCrossMethodFinding(
        call: FunctionCall,
        paramName: String,
        loopStack: List<LoopNode>,
    ): Finding {
        val outerLoop = loopStack.first()
        val loopVar = outerLoop.iteratedVariable ?: "items"
        return Finding(
            ruleId = id,
            ruleName = name,
            severity = severity,
            location = call.location,
            message =
                "Parameter '$paramName' flows through ${call.name}() into IO " +
                    "inside ${outerLoop.kind.label()}",
            suggestion =
                "Batch the IO operation outside the loop, " +
                    "or restructure ${call.name}() to accept a collection",
            currentComplexity = "O(|$loopVar| \u00d7 IO)",
            suggestedComplexity = "O(1) IO + O(|$loopVar|)",
            evidence =
                listOf(
                    Evidence(
                        outerLoop.location,
                        outerLoop.kind.label(),
                        ExecutionContext.INSIDE_LOOP,
                        complexity = "O(|$loopVar|)",
                    ),
                    Evidence(
                        call.location,
                        "${call.name}() called per iteration — param '$paramName' flows to IO",
                        ExecutionContext.INSIDE_LOOP,
                        depth = 1,
                        complexity = "IO \u2190 bottleneck",
                    ),
                ),
        )
    }

    private fun buildEvidence(
        call: FunctionCall,
        outerLoop: LoopNode,
        loopVar: String,
    ): List<Evidence> =
        listOf(
            Evidence(
                outerLoop.location,
                outerLoop.kind.label(),
                ExecutionContext.INSIDE_LOOP,
                complexity = "O(|$loopVar|)",
            ),
            Evidence(
                call.location,
                "${call.name}() inside loop",
                ExecutionContext.INSIDE_LOOP,
                depth = 1,
                complexity = "IO \u2190 bottleneck",
            ),
        )
}

/** Returns true if the call target is a known in-memory buffer (not real IO). */
private fun isInMemoryTarget(
    call: FunctionCall,
    language: Language,
    registry: LanguageSemanticsRegistry,
): Boolean {
    val target = call.qualifiedTarget?.lowercase() ?: return false
    return registry.nonIoTargets(language).any { target.contains(it) }
}

/**
 * Returns true if the call target matches an IO-capable receiver pattern.
 * Patterns prefixed with `*` match anywhere (contains), others match as suffix (endsWith).
 * This prevents "session" from matching "sessionState" while still matching "hibernateSession".
 */
private fun isIOTarget(
    call: FunctionCall,
    language: Language,
    registry: LanguageSemanticsRegistry,
): Boolean {
    val target = call.qualifiedTarget?.lowercase() ?: return false
    return registry.ioTargetPatterns(language).any { pattern ->
        if (pattern.startsWith("*")) {
            target.contains(pattern.removePrefix("*"))
        } else {
            target.endsWith(pattern) || target == pattern
        }
    }
}
