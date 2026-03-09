package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.model.ExecutionContext
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LookupCall
import com.github.tvinke.algorilla.model.LookupKind
import com.github.tvinke.algorilla.model.LoopNode
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.ComplexityModel
import com.github.tvinke.algorilla.rules.Evidence
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.Rule
import com.github.tvinke.algorilla.rules.RuleCategory
import com.github.tvinke.algorilla.util.CrossMethodResolver
import com.github.tvinke.algorilla.util.hasO1Type

/**
 * Detects linear lookup operations (contains, indexOf, find, filter, etc.) inside loop bodies
 * or inside iterating higher-order functions (findAll, any, every, etc.).
 *
 * When a collection is searched linearly on every iteration, the combined complexity
 * becomes O(n*m) or O(n^2) where O(n) would suffice with a pre-built Set or Map.
 */
public class NestedLookupRule : Rule {
    override val id: String = "nested-lookup"
    override val name: String = "Nested Lookup"
    override val severity: Severity = Severity.WARNING
    override val languages: Set<Language> = Language.entries.toSet()
    override val category: RuleCategory = RuleCategory.LOOP_AMPLIFIER

    override fun evaluate(context: AnalysisContext): List<Finding> {
        val findings = mutableListOf<Finding>()
        for ((_, fileRoot) in context.irTrees) {
            scanNode(fileRoot, null, emptyList(), context, findings)
        }
        return findings
    }

    private fun scanNode(
        node: IRNode,
        enclosingFn: FunctionDecl?,
        iterationStack: List<IRNode>,
        context: AnalysisContext,
        findings: MutableList<Finding>,
    ) {
        val fn = if (node is FunctionDecl) node else enclosingFn

        if (node is LoopNode) {
            for (child in node.children) {
                scanNode(child, fn, iterationStack + node, context, findings)
            }
            return
        }

        if (node is LookupCall) {
            if (iterationStack.isNotEmpty() && !isO1Lookup(node, fn)) {
                findings.add(buildFinding(node, iterationStack))
            }
            if (node.children.isNotEmpty() && isIteratingLookup(node.kind)) {
                for (child in node.children) {
                    scanNode(child, fn, iterationStack + node, context, findings)
                }
                return
            }
        }

        checkCrossMethod(node, iterationStack, context, findings)

        for (child in node.children) {
            scanNode(child, fn, iterationStack, context, findings)
        }
    }

    private fun checkCrossMethod(
        node: IRNode,
        iterationStack: List<IRNode>,
        context: AnalysisContext,
        findings: MutableList<Finding>,
    ) {
        if (iterationStack.isEmpty() || node !is FunctionCall) return
        val maxDepth = context.config.maxCallDepth.coerceAtMost(2)
        val hiddenLookup =
            CrossMethodResolver.resolveAndFind<LookupCall>(
                node,
                context.symbolTable,
                maxDepth = maxDepth,
            ) { !it.isO1 }
        if (hiddenLookup != null) {
            findings.add(buildCrossMethodFinding(node, hiddenLookup, iterationStack))
        }
    }

    private fun isO1Lookup(
        lookup: LookupCall,
        fn: FunctionDecl?,
    ): Boolean = lookup.isO1 || (fn != null && fn.hasO1Type(lookup.targetVariable))

    private fun buildFinding(
        lookup: LookupCall,
        iterationStack: List<IRNode>,
    ): Finding {
        val targetVar = lookup.targetVariable ?: "collection"
        val outerVar = iteratedVar(iterationStack.first())
        val outerIteration = iterationStack.first()
        val evidence = buildEvidence(iterationStack, lookup, targetVar)
        val cx = ComplexityModel.loopTimesLookup(outerVar, targetVar)

        return Finding(
            ruleId = id,
            ruleName = name,
            severity = severity,
            location = lookup.location,
            message = "Linear ${lookup.kind.label} on '$targetVar' inside ${iterationLabel(outerIteration)}",
            suggestion = "Build a HashSet/Map from '$targetVar' before the loop",
            currentComplexity = cx.current,
            suggestedComplexity = cx.suggested,
            evidence = evidence,
        )
    }

    private fun buildCrossMethodFinding(
        call: FunctionCall,
        hiddenLookup: LookupCall,
        iterationStack: List<IRNode>,
    ): Finding {
        val outerIteration = iterationStack.first()
        val outerVar = iteratedVar(outerIteration)
        val targetVar = hiddenLookup.targetVariable ?: "collection"
        val evidence = buildCrossMethodEvidence(iterationStack, call, hiddenLookup, targetVar)
        val lookupDesc = hiddenLookup.kind.label
        val msg =
            "Linear $lookupDesc on '$targetVar' inside " +
                "${call.name}() called from ${iterationLabel(outerIteration)}"
        val cx = ComplexityModel.loopTimesLookup(outerVar, targetVar)
        return Finding(
            ruleId = id,
            ruleName = name,
            severity = severity,
            location = call.location,
            message = msg,
            suggestion = "Build a HashSet/Map from '$targetVar' before the loop",
            currentComplexity = cx.current,
            suggestedComplexity = cx.suggested,
            evidence = evidence,
        )
    }

    private fun buildCrossMethodEvidence(
        iterationStack: List<IRNode>,
        call: FunctionCall,
        hiddenLookup: LookupCall,
        targetVar: String,
    ): List<Evidence> =
        iterationStack.mapIndexed { idx, node ->
            val varName = iteratedVar(node)
            Evidence(
                location = node.location,
                label = "${iterationLabel(node)} over $varName",
                executionContext = ExecutionContext.INSIDE_LOOP,
                depth = idx,
                complexity = ComplexityModel.loopEvidence(varName),
            )
        } +
            listOf(
                Evidence(call.location, "${call.name}() called per iteration", ExecutionContext.INSIDE_LOOP, depth = iterationStack.size),
                Evidence(
                    hiddenLookup.location,
                    "linear ${hiddenLookup.kind.label} on '$targetVar' inside ${call.name}()",
                    ExecutionContext.INSIDE_LOOP,
                    depth = iterationStack.size + 1,
                    complexity = ComplexityModel.bottleneckO(targetVar),
                ),
            )

    private fun buildEvidence(
        iterationStack: List<IRNode>,
        lookup: LookupCall,
        targetVar: String,
    ): List<Evidence> {
        val evidence =
            iterationStack.mapIndexed { idx, node ->
                val varName = iteratedVar(node)
                Evidence(
                    location = node.location,
                    label = "${iterationLabel(node)} over $varName",
                    executionContext = ExecutionContext.INSIDE_LOOP,
                    depth = idx,
                    complexity = ComplexityModel.loopEvidence(varName),
                )
            }
        return evidence +
            Evidence(
                location = lookup.location,
                label = "${lookup.kind.label} on '$targetVar'",
                executionContext = ExecutionContext.INSIDE_LOOP,
                depth = iterationStack.size,
                complexity = ComplexityModel.bottleneckO(targetVar),
            )
    }
}

/**
 * LookupKinds that iterate over a collection (their closure body runs per element).
 */
private val ITERATING_LOOKUP_KINDS =
    setOf(
        LookupKind.FIND,
        LookupKind.FILTER,
        LookupKind.ANY_MATCH,
        LookupKind.ALL_MATCH,
        LookupKind.NONE_MATCH,
        LookupKind.SOME,
        LookupKind.COUNT,
    )

private fun isIteratingLookup(kind: LookupKind): Boolean = kind in ITERATING_LOOKUP_KINDS

private fun iterationLabel(node: IRNode): String =
    when (node) {
        is LoopNode -> node.kind.label()
        is LookupCall -> "${node.kind.label}()"
        else -> "iteration"
    }

private fun iteratedVar(node: IRNode): String =
    when (node) {
        is LoopNode -> node.iteratedVariable ?: "items"
        is LookupCall -> node.targetVariable ?: "collection"
        else -> "items"
    }

internal fun com.github.tvinke.algorilla.model.LoopKind.label(): String =
    when (this) {
        com.github.tvinke.algorilla.model.LoopKind.FOR -> "for loop"
        com.github.tvinke.algorilla.model.LoopKind.WHILE -> "while loop"
        com.github.tvinke.algorilla.model.LoopKind.FOR_EACH -> "for-each loop"
        com.github.tvinke.algorilla.model.LoopKind.STREAM_FOR_EACH -> "stream().forEach()"
        com.github.tvinke.algorilla.model.LoopKind.HIGHER_ORDER -> "forEach()"
    }
