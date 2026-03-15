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
import com.github.tvinke.algorilla.util.findDescendants
import com.github.tvinke.algorilla.util.isRecursive

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

    @Suppress("ReturnCount")
    private fun checkForHiddenLoop(
        call: FunctionCall,
        callerFn: FunctionDecl?,
        loopStack: List<LoopNode>,
        language: Language,
        context: AnalysisContext,
        findings: MutableList<Finding>,
    ) {
        if (isStringOrCopyMethod(call.name, language, context.registry)) return

        val resolved = CrossMethodResolver.resolve(call, context.symbolTable) ?: return

        // Skip recursive methods — their internal loop iterates child nodes
        // of the same data structure, not an independent collection
        if (resolved.isRecursive()) return

        val hiddenLoop = resolved.findDescendants<LoopNode>().firstOrNull() ?: return

        // Skip trivial methods (single-statement wrappers with no real loop body)
        if (isTrivialLoop(hiddenLoop, language, context.registry)) return

        // Flow-based confidence: if a parameter flows through this call into a loop
        // in the callee, we have proof the nested iteration is on caller data
        val flowConfirmed =
            callerFn != null &&
                ParameterFlowQuery.parameterFlowsThrough(
                    call,
                    callerFn,
                    context.symbolTable,
                ) { it is FlowTarget.LoopIteration } != null

        findings.add(buildFinding(call, resolved, hiddenLoop, loopStack, flowConfirmed))
    }

    private fun buildFinding(
        call: FunctionCall,
        resolved: FunctionDecl,
        hiddenLoop: LoopNode,
        loopStack: List<LoopNode>,
        flowConfirmed: Boolean = false,
    ): Finding {
        val outerVar = (loopStack.first().iteratedVariable ?: "items")
        val innerVar = hiddenLoop.iteratedVariable ?: "elements"
        val cx = ComplexityModel.loopTimesLookup(outerVar, innerVar)
        return Finding(
            ruleId = id,
            ruleName = name,
            severity = severity,
            confidence = if (flowConfirmed) Confidence.HIGH else Confidence.MEDIUM,
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
    val lower = name.lowercase()
    if (registry.hiddenLoopSkipPrefixes(language).any { lower.startsWith(it) }) return true
    return registry.hiddenLoopSkipKeywords(language).any { lower.contains(it) }
}
