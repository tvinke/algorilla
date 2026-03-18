package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.model.Confidence
import com.github.tvinke.algorilla.model.ExecutionContext
import com.github.tvinke.algorilla.model.FlowTarget
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LookupCall
import com.github.tvinke.algorilla.model.LoopKind
import com.github.tvinke.algorilla.model.LoopNode
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Evidence
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.Rule
import com.github.tvinke.algorilla.rules.RuleCategory
import com.github.tvinke.algorilla.rules.Suggestion
import com.github.tvinke.algorilla.semantics.LanguageSemanticsRegistry
import com.github.tvinke.algorilla.util.ParameterFlowQuery
import com.github.tvinke.algorilla.util.findDescendants

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
@Suppress("LargeClass") // Cohesive rule: scan + classify + build findings for one anti-pattern
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

    @Suppress("LongParameterList", "CyclomaticComplexMethod")
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
            // Stream copy idiom: read(buffer) + write(buffer) in a loop is the correct
            // pattern for copying data between streams — not an anti-pattern.
            if (isStreamCopyLoop(node)) return
            for (child in node.children) {
                scanNode(child, fn, loopStack + node, ioMethods, ioCandidates, language, context, findings)
            }
            return
        }

        // Treat stream pipeline ops (map, flatMap, filter) as implicit iteration contexts.
        // Their lambda arguments are executed per-element, just like a loop body.
        // Exception: monadic single-item types (Mono, Uni, Optional, CompletableFuture) —
        // flatMap on these is a 1-to-1 transformation, not iteration over a collection.
        val isImplicitIteration =
            node is FunctionCall &&
                node.name in context.registry.implicitIterationOps(language) &&
                !isMonadicTarget(node, language, context.registry)
        if (isImplicitIteration) {
            val syntheticLoop = LoopNode(LoopKind.HIGHER_ORDER, node.qualifiedTarget, node.location, node.children)
            for (child in node.children) {
                scanNode(child, fn, loopStack + syntheticLoop, ioMethods, ioCandidates, language, context, findings)
            }
            return
        }

        if (loopStack.isNotEmpty()) {
            checkNodeInLoop(node, fn, loopStack, ioMethods, ioCandidates, language, context, findings)
        }

        for (child in node.children) {
            scanNode(child, fn, loopStack, ioMethods, ioCandidates, language, context, findings)
        }
    }

    @Suppress("LongParameterList", "LongMethod")
    private fun checkNodeInLoop(
        node: IRNode,
        fn: FunctionDecl?,
        loopStack: List<LoopNode>,
        ioMethods: Set<String>,
        ioCandidates: Set<String>,
        language: Language,
        context: AnalysisContext,
        findings: MutableList<Finding>,
    ) {
        if (node is FunctionCall) {
            val isDefiniteIO = node.name in ioMethods && !isInMemoryTarget(node, language, context.registry)
            val isCandidateIO =
                !isDefiniteIO &&
                    node.name in ioCandidates &&
                    !isInMemoryTarget(node, language, context.registry) &&
                    isIOTarget(node, language, context.registry)

            if (isDefiniteIO || isCandidateIO) {
                findings.add(buildFinding(node, loopStack, hasLoopParamFlow(fn), isDefiniteIO))
            } else if (fn != null) {
                checkCrossMethodIO(node, fn, loopStack, language, context, findings)
            }
        }

        // Safety net: LookupCall nodes (e.g. find()) that target IO-capable receivers
        // are likely DB/API calls misclassified as collection lookups.
        if (node is LookupCall) {
            val methodName = node.kind.label
            if (methodName in ioCandidates && isIOTargetLookup(node, language, context.registry)) {
                findings.add(buildLookupFinding(node, loopStack, hasLoopParamFlow(fn)))
            }
        }
    }

    private fun hasLoopParamFlow(fn: FunctionDecl?): Boolean =
        fn != null &&
            fn.parameterFlows.any { flow ->
                flow.flowsInto.any { it is FlowTarget.LoopIteration }
            }

    private fun checkCrossMethodIO(
        call: FunctionCall,
        callerFn: FunctionDecl,
        loopStack: List<LoopNode>,
        language: Language,
        context: AnalysisContext,
        findings: MutableList<Finding>,
    ) {
        val languageIoMethods = context.registry.ioMethods(language)
        val evidence =
            ParameterFlowQuery.parameterFlowsThrough(
                call = call,
                callerFn = callerFn,
                symbolTable = context.symbolTable,
                maxDepth = context.config.maxCallDepth.coerceAtMost(2),
            ) { target ->
                // Check if the terminal operation is a method call to an IO method
                target is FlowTarget.MethodCallReceiver &&
                    target.methodName in languageIoMethods
            }
        if (evidence != null) {
            findings.add(buildCrossMethodFinding(call, evidence.paramName, loopStack))
        }
    }

    private fun buildFinding(
        call: FunctionCall,
        loopStack: List<LoopNode>,
        flowConfirmed: Boolean = false,
        isUnambiguousIO: Boolean = false,
    ): Finding {
        val outerLoop = loopStack.first()
        val loopVar = outerLoop.iteratedVariable ?: "items"
        // Unambiguous io-methods (executeQuery, getForObject, etc.) are almost certainly real IO.
        // Candidate methods (save, delete) need target confirmation and stay MEDIUM.
        val highConfidence = flowConfirmed || isUnambiguousIO
        return Finding(
            ruleId = id,
            ruleName = name,
            severity = severity,
            confidence = if (highConfidence) Confidence.HIGH else Confidence.MEDIUM,
            location = call.location,
            message =
                "IO call ${call.name}() inside ${outerLoop.kind.label()} — " +
                    "each iteration incurs network/disk latency",
            suggestions =
                listOf(
                    Suggestion.Freeform(
                        "Batch the IO operation outside the loop, " +
                            "or use a bulk API (e.g. saveAll, findAllById)",
                    ),
                ),
            currentComplexity = "O($loopVar \u00d7 IO)",
            suggestedComplexity = "O(1) IO + O($loopVar)",
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
            suggestions =
                listOf(
                    Suggestion.Freeform(
                        "Batch the IO operation outside the loop, " +
                            "or restructure ${call.name}() to accept a collection",
                    ),
                ),
            currentComplexity = "O($loopVar \u00d7 IO)",
            suggestedComplexity = "O(1) IO + O($loopVar)",
            evidence =
                listOf(
                    Evidence(
                        outerLoop.location,
                        outerLoop.kind.label(),
                        ExecutionContext.INSIDE_LOOP,
                        complexity = "O($loopVar)",
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
                complexity = "O($loopVar)",
            ),
            Evidence(
                call.location,
                "${call.name}() inside loop",
                ExecutionContext.INSIDE_LOOP,
                depth = 1,
                complexity = "IO \u2190 bottleneck",
            ),
        )

    @Suppress("LongMethod")
    private fun buildLookupFinding(
        call: LookupCall,
        loopStack: List<LoopNode>,
        flowConfirmed: Boolean = false,
    ): Finding {
        val outerLoop = loopStack.first()
        val loopVar = outerLoop.iteratedVariable ?: "items"
        val methodName = call.kind.label
        return Finding(
            ruleId = id,
            ruleName = name,
            severity = severity,
            confidence = if (flowConfirmed) Confidence.HIGH else Confidence.MEDIUM,
            location = call.location,
            message =
                "IO call $methodName() inside ${outerLoop.kind.label()} — " +
                    "each iteration incurs network/disk latency",
            suggestions =
                listOf(
                    Suggestion.Freeform(
                        "Batch the IO operation outside the loop, " +
                            "or use a bulk API (e.g. findAll, findAllById)",
                    ),
                ),
            currentComplexity = "O($loopVar \u00d7 IO)",
            suggestedComplexity = "O(1) IO + O($loopVar)",
            evidence =
                listOf(
                    Evidence(
                        outerLoop.location,
                        outerLoop.kind.label(),
                        ExecutionContext.INSIDE_LOOP,
                        complexity = "O($loopVar)",
                    ),
                    Evidence(
                        call.location,
                        "$methodName() inside loop",
                        ExecutionContext.INSIDE_LOOP,
                        depth = 1,
                        complexity = "IO \u2190 bottleneck",
                    ),
                ),
        )
    }
}

/**
 * Detects the standard stream copy idiom: a loop containing both `read(buffer)` and `write(buffer)`
 * on stream-like targets. This is the correct pattern for copying data between streams and
 * should not be flagged as IO-in-loop.
 */
private fun isStreamCopyLoop(loop: LoopNode): Boolean {
    val calls = loop.findDescendants<FunctionCall>()
    val hasRead = calls.any { it.name == "read" && it.arguments.isNotEmpty() }
    val hasWrite = calls.any { it.name == "write" && it.arguments.isNotEmpty() }
    return hasRead && hasWrite
}

/** Returns true if this call's target is a monadic single-item type (not a collection). */
private fun isMonadicTarget(
    call: FunctionCall,
    language: Language,
    registry: LanguageSemanticsRegistry,
): Boolean {
    val target = call.qualifiedTarget ?: return false
    return registry.isMonadicTarget(language, target)
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
): Boolean = matchesIOPattern(call.qualifiedTarget, language, registry)

/** Overload for LookupCall nodes (same pattern matching, different target field). */
private fun isIOTargetLookup(
    call: LookupCall,
    language: Language,
    registry: LanguageSemanticsRegistry,
): Boolean = matchesIOPattern(call.targetVariable, language, registry)

private fun matchesIOPattern(
    target: String?,
    language: Language,
    registry: LanguageSemanticsRegistry,
): Boolean {
    val t = target?.lowercase() ?: return false
    return registry.ioTargetPatterns(language).any { pattern ->
        if (pattern.startsWith("*")) {
            t.contains(pattern.removePrefix("*"))
        } else {
            t.endsWith(pattern) || t == pattern
        }
    }
}
