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
import com.github.tvinke.algorilla.rules.ComplexityModel
import com.github.tvinke.algorilla.rules.Evidence
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.Rule
import com.github.tvinke.algorilla.rules.RuleCategory
import com.github.tvinke.algorilla.rules.Suggestion
import com.github.tvinke.algorilla.semantics.LanguageSemanticsRegistry
import com.github.tvinke.algorilla.util.CrossMethodResolver
import com.github.tvinke.algorilla.util.ParameterFlowQuery
import com.github.tvinke.algorilla.util.ResolutionConfidence
import com.github.tvinke.algorilla.util.ResolutionResult
import com.github.tvinke.algorilla.util.containsAnyAtWordBoundary
import com.github.tvinke.algorilla.util.demoteIfAmbiguous
import com.github.tvinke.algorilla.util.findDescendants
import com.github.tvinke.algorilla.util.isRecursive
import com.github.tvinke.algorilla.util.startsWithAtWordBoundary
import com.github.tvinke.algorilla.util.worstOf

/**
 * Detects loops hidden behind method calls: when a loop calls a method that internally
 * contains another loop, the combined complexity is O(outer × inner) but looks like O(n).
 *
 * Only reports when the called method can be resolved via the symbol table and contains
 * a LoopNode in its body. Does not flag trivial/pure methods or methods already covered
 * by more specific rules (e.g. nested-lookup for contains() inside loops).
 */
public class HiddenNestedLoopRule : Rule {
    override val id: String = "hidden-nested-loop"
    override val name: String = "Hidden Nested Loop"
    override val severity: Severity = Severity.WARNING
    override val languages: Set<Language> = Language.entries.toSet()
    override val category: RuleCategory = RuleCategory.LOOP_AMPLIFIER

    override fun evaluate(context: AnalysisContext): List<Finding> {
        val findings = mutableListOf<Finding>()
        for ((_, fileRoot) in context.irTrees) {
            scanNode(fileRoot, null, emptyList(), fileRoot.language, context, findings)
        }
        return findings
    }

    private fun scanNode(
        node: IRNode,
        enclosingFn: FunctionDecl?,
        loopStack: List<LoopNode>,
        language: Language,
        context: AnalysisContext,
        findings: MutableList<Finding>,
    ) {
        val fn = if (node is FunctionDecl) node else enclosingFn

        if (node is LoopNode) {
            for (child in node.children) {
                scanNode(child, fn, loopStack + node, language, context, findings)
            }
            return
        }

        if (loopStack.isNotEmpty() && node is FunctionCall) {
            checkForHiddenLoop(node, fn, loopStack, language, context, findings)
        }

        for (child in node.children) {
            scanNode(child, fn, loopStack, language, context, findings)
        }
    }

    @Suppress("ReturnCount") // Guard clauses with early returns — clearer than nested if/else
    private fun checkForHiddenLoop(
        call: FunctionCall,
        callerFn: FunctionDecl?,
        loopStack: List<LoopNode>,
        language: Language,
        context: AnalysisContext,
        findings: MutableList<Finding>,
    ) {
        if (isStringOrCopyMethod(call.name, language, context.registry)) return

        val (resolved, resolutionConfidence) =
            when (val result = CrossMethodResolver.resolve(call, context.symbolTable, language)) {
                is ResolutionResult.Unresolved -> return
                is ResolutionResult.Resolved -> result.decl to result.confidence
            }

        // Skip recursive methods — their internal loop iterates child nodes
        // of the same data structure, not an independent collection.
        // Recomputed here rather than reading the cached FunctionDecl.isRecursive property:
        // rule-level tests build an AnalysisContext directly without running
        // AnalysisEngine.annotateRecursion first, so the cached value can't be trusted.
        if (resolved.isRecursive(context.symbolTable)) return

        val hiddenLoop = resolved.findDescendants<LoopNode>().firstOrNull() ?: return

        // Skip trivial methods (single-statement wrappers with no real loop body)
        if (isTrivialLoop(hiddenLoop, language, context.registry)) return

        // Skip when the hidden loop iterates a constant-bound collection
        // (e.g. enum values, mappers, validators) — O(n*k) with small k
        if (hiddenLoop.isConstantBound) return

        // Skip when the outer loop itself is constant-bound (enum iteration) —
        // the hidden nested loop is O(k*m) with constant k
        if (loopStack.all { it.isConstantBound }) return

        // Flow-based confidence: if a parameter flows through this call into a loop
        // in the callee, we have proof the nested iteration is on caller data. The flow
        // chain can itself cross an ambiguous overload guess at a hop deeper than `call`
        // (parameterFlowsThrough follows further calls up to its own maxDepth) - folding its
        // resolutionConfidence in via worstOf means a guess two hops into the flow still
        // gets caught, not just an ambiguous `call` itself.
        val flowEvidence =
            callerFn?.let {
                ParameterFlowQuery.parameterFlowsThrough(call, it, context.symbolTable) { target ->
                    target is FlowTarget.LoopIteration
                }
            }
        val flowConfirmed = flowEvidence != null
        val overallConfidence =
            flowEvidence?.let { worstOf(resolutionConfidence, it.resolutionConfidence) } ?: resolutionConfidence

        findings.add(buildFinding(call, resolved, hiddenLoop, loopStack, flowConfirmed, overallConfidence))
    }

    private fun buildFinding(
        call: FunctionCall,
        resolved: FunctionDecl,
        hiddenLoop: LoopNode,
        loopStack: List<LoopNode>,
        flowConfirmed: Boolean = false,
        resolutionConfidence: ResolutionConfidence = ResolutionConfidence.EXACT,
    ): Finding {
        val outerVar = (loopStack.first().iteratedVariable ?: "items")
        val innerVar = hiddenLoop.iteratedVariable ?: "elements"
        val cx = ComplexityModel.loopTimesLookup(outerVar, innerVar)
        // An ambiguous overload guess means "resolved" might not be the function the call
        // actually targets - the hidden loop we're reporting could belong to the wrong
        // overload entirely, so never report higher than LOW on a guess, flow-confirmed or not.
        val confidence = resolutionConfidence.demoteIfAmbiguous(if (flowConfirmed) Confidence.HIGH else Confidence.MEDIUM)
        return Finding(
            ruleId = id,
            ruleName = name,
            severity = severity,
            confidence = confidence,
            location = call.location,
            message =
                "${call.name}() contains a ${hiddenLoop.kind.label()} \u2014 " +
                    "hidden O($outerVar \u00d7 $innerVar) complexity",
            suggestions =
                listOf(
                    Suggestion.Freeform(
                        "Consider inlining the loop, batching the work, " +
                            "or pre-building a lookup structure in ${resolved.name}()",
                    ),
                ),
            currentComplexity = cx.current,
            suggestedComplexity = cx.suggested,
            evidence = buildEvidence(call, resolved, hiddenLoop, loopStack, innerVar),
        )
    }

    private fun buildEvidence(
        call: FunctionCall,
        resolved: FunctionDecl,
        hiddenLoop: LoopNode,
        loopStack: List<LoopNode>,
        innerVar: String,
    ): List<Evidence> =
        loopStack.mapIndexed { idx, loop ->
            val varName = loop.iteratedVariable ?: "items"
            Evidence(
                location = loop.location,
                label = "${loop.kind.label()} over $varName",
                executionContext = ExecutionContext.INSIDE_LOOP,
                depth = idx,
                complexity = ComplexityModel.loopEvidence(varName),
            )
        } +
            listOf(
                Evidence(
                    call.location,
                    "${call.name}() called per iteration",
                    ExecutionContext.INSIDE_LOOP,
                    depth = loopStack.size,
                ),
                Evidence(
                    hiddenLoop.location,
                    "${hiddenLoop.kind.label()} over $innerVar inside ${resolved.name}()",
                    ExecutionContext.INSIDE_LOOP,
                    depth = loopStack.size + 1,
                    complexity = ComplexityModel.bottleneckO(innerVar),
                ),
            )
}

/** Filter out trivial loops with no meaningful body or only simple operations. */
private fun isTrivialLoop(
    loop: LoopNode,
    language: Language,
    registry: LanguageSemanticsRegistry,
): Boolean {
    if (loop.children.isEmpty()) return true
    // A loop with a single child that is a simple function call to a skip/trivial method
    if (loop.children.size == 1) {
        val child = loop.children.first()
        if (child is FunctionCall && isStringOrCopyMethod(child.name, language, registry)) return true
    }
    return false
}

/**
 * Returns true if the method name is a known string/byte iterator or collection-copy
 * operation that should not be flagged as a hidden nested loop.
 */
private fun isStringOrCopyMethod(
    name: String,
    language: Language,
    registry: LanguageSemanticsRegistry,
): Boolean {
    if (name in registry.hiddenLoopSkipMethods(language)) return true
    if (registry.hiddenLoopSkipPrefixes(language).any { startsWithAtWordBoundary(name, it) }) return true
    // Original-case name for the boundary check - lowercasing first would destroy the
    // camelCase signal a boundary check needs. "processCharge" contains "char" but isn't a
    // char-iteration method - the trailing boundary catches it (the "ge" after "Char" is a
    // lowercase continuation, not a new word), the same way "screenwriter" doesn't match
    // "writer" elsewhere in this campaign.
    return containsAnyAtWordBoundary(name, registry.hiddenLoopSkipKeywords(language))
}
