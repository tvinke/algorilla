package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.model.Confidence
import com.github.tvinke.algorilla.model.ExecutionContext
import com.github.tvinke.algorilla.model.FlowTarget
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LookupCall
import com.github.tvinke.algorilla.model.LookupKind
import com.github.tvinke.algorilla.model.LoopKind
import com.github.tvinke.algorilla.model.LoopNode
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.ComplexityModel
import com.github.tvinke.algorilla.rules.Evidence
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.Rule
import com.github.tvinke.algorilla.rules.RuleCategory
import com.github.tvinke.algorilla.util.CrossMethodResolver
import com.github.tvinke.algorilla.util.findDescendants
import com.github.tvinke.algorilla.util.isCollectionLookup
import com.github.tvinke.algorilla.util.isRecursive

/**
 * Detects linear lookup operations (contains, indexOf, find, filter, etc.) inside loop bodies
 * or inside iterating higher-order functions (findAll, any, every, etc.).
 *
 * When a collection is searched linearly on every iteration, the combined complexity
 * becomes O(n*m) or O(n^2) where O(n) would suffice with a pre-built Set or Map.
 */
@Suppress("LargeClass")
public class NestedLookupRule : Rule {
    override val id: String = "nested-lookup"
    override val name: String = "Nested Lookup"
    override val severity: Severity = Severity.WARNING
    override val languages: Set<Language> = Language.entries.toSet()
    override val category: RuleCategory = RuleCategory.LOOP_AMPLIFIER
    override val subsumes: Set<String> = setOf("expensive-callback")

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
            val typeEnv = fn?.let { context.typeEnvironmentFor(it) }
            if (iterationStack.isNotEmpty() && node.isCollectionLookup(fn, typeEnv)) {
                findings.add(buildFinding(node, iterationStack, fn))
            }
            if (node.children.isNotEmpty() && isIteratingLookup(node.kind)) {
                for (child in node.children) {
                    scanNode(child, fn, iterationStack + node, context, findings)
                }
                return
            }
        }

        checkCrossMethod(node, fn, iterationStack, context, findings)

        for (child in node.children) {
            scanNode(child, fn, iterationStack, context, findings)
        }
    }

    private fun checkCrossMethod(
        node: IRNode,
        enclosingFn: FunctionDecl?,
        iterationStack: List<IRNode>,
        context: AnalysisContext,
        findings: MutableList<Finding>,
    ) {
        if (iterationStack.isEmpty() || node !is FunctionCall) return

        // Skip tree-walk patterns: recursive functions called from higher-order iteration
        if (isTreeWalkCall(node, enclosingFn, iterationStack, context.symbolTable)) return

        val maxDepth = context.config.maxCallDepth.coerceAtMost(2)
        val hiddenLookup =
            CrossMethodResolver.resolveAndFind<LookupCall>(
                node,
                context.symbolTable,
                maxDepth = maxDepth,
            ) { it.isCollectionLookup(null) }
        if (hiddenLookup != null) {
            val confidence = crossMethodConfidence(node, hiddenLookup, iterationStack, enclosingFn)
            findings.add(buildCrossMethodFinding(node, hiddenLookup, iterationStack, confidence))
        }
    }

    /**
     * Determines confidence for a cross-method nested lookup.
     * When the call target is an external object (not a loop variable and not this/super),
     * the hidden lookup is on the callee's internal data — demote to LOW.
     * When parameter flow proves the loop variable reaches the hidden lookup — HIGH.
     */
    @Suppress("ReturnCount")
    private fun crossMethodConfidence(
        call: FunctionCall,
        hiddenLookup: LookupCall,
        iterationStack: List<IRNode>,
        enclosingFn: FunctionDecl?,
    ): Confidence {
        // If the lookup target matches a loop-iterated variable, it's likely a real nested lookup
        val loopVars = iterationStack.mapNotNull { iteratedVarOf(it) }.toSet()
        if (hiddenLookup.targetVariable in loopVars) return Confidence.MEDIUM

        // Call on an external object (not this/super, not a loop variable) — the hidden lookup
        // operates on the callee's own data, not the loop's collection
        val target = call.qualifiedTarget
        if (target != null && target !in SELF_REFERENCES && target !in loopVars) {
            return Confidence.LOW
        }

        // Parameter-backed: if the lookup target comes from a caller parameter
        if (enclosingFn != null && hiddenLookup.targetVariable != null) {
            if (isParamBacked(hiddenLookup.targetVariable!!, enclosingFn)) return Confidence.HIGH
        }

        return Confidence.MEDIUM
    }

    private fun iteratedVarOf(node: IRNode): String? =
        when (node) {
            is LoopNode -> node.iteratedVariable
            is LookupCall -> node.targetVariable
            else -> null
        }

    private companion object {
        private val SELF_REFERENCES = setOf("this", "super")
    }

    /**
     * Detects tree-walk/transform patterns that should not be flagged as nested lookups.
     * A call is a tree-walk when it's inside a higher-order iteration (.map/.forEach) and:
     * 1. It's a direct self-recursion (processModule calls processModule), or
     * 2. The resolved function is itself recursive (processPackage calls processPackage), or
     * 3. Mutual 2-cycle: resolved function calls back to the enclosing function.
     */
    @Suppress("ReturnCount")
    private fun isTreeWalkCall(
        call: FunctionCall,
        enclosingFn: FunctionDecl?,
        iterationStack: List<IRNode>,
        symbolTable: com.github.tvinke.algorilla.graph.SymbolTable,
    ): Boolean {
        // Only applies inside higher-order iteration (.map{}, .forEach{})
        val outerLoop = iterationStack.lastOrNull()
        if (outerLoop !is LoopNode || outerLoop.kind != LoopKind.HIGHER_ORDER) return false

        // Case 1: direct self-recursion (processModule calls processModule)
        if (enclosingFn != null && call.name == enclosingFn.name) return true

        // Case 2: resolved function is itself recursive (processPackage calls processPackage)
        val resolved = CrossMethodResolver.resolve(call, symbolTable) ?: return false
        if (resolved.isRecursive()) return true

        // Case 3: mutual recursion — resolved function calls back to enclosing function
        if (enclosingFn != null) {
            val callsBack = resolved.findDescendants<FunctionCall>().any { it.name == enclosingFn.name }
            if (callsBack) return true
        }

        return false
    }

    private fun buildFinding(
        lookup: LookupCall,
        iterationStack: List<IRNode>,
        enclosingFn: FunctionDecl? = null,
    ): Finding {
        val targetVar = lookup.targetVariable ?: "collection"
        val outerVar = iteratedVar(iterationStack.first())
        val outerIteration = iterationStack.first()
        val evidence = buildEvidence(iterationStack, lookup, targetVar)
        val cx = ComplexityModel.loopTimesLookup(outerVar, targetVar)

        // Flow-based confidence: if the lookup target is a parameter (or alias of one),
        // we have proof it's a collection being scanned, not a scalar or O(1) type
        val paramBacked = enclosingFn != null && isParamBacked(targetVar, enclosingFn)

        return Finding(
            ruleId = id,
            ruleName = name,
            severity = severity,
            confidence = if (paramBacked) Confidence.HIGH else Confidence.MEDIUM,
            location = lookup.location,
            message = "Linear ${lookup.kind.label} on '$targetVar' inside ${iterationLabel(outerIteration)}",
            suggestion = "Build a ${lookup.kind.suggestedStructure()} from '$targetVar' before the loop",
            currentComplexity = cx.current,
            suggestedComplexity = cx.suggested,
            evidence = evidence,
        )
    }

    private fun buildCrossMethodFinding(
        call: FunctionCall,
        hiddenLookup: LookupCall,
        iterationStack: List<IRNode>,
        confidence: Confidence = Confidence.MEDIUM,
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
            confidence = confidence,
            location = call.location,
            message = msg,
            suggestion = "Build a ${hiddenLookup.kind.suggestedStructure()} from '$targetVar' before the loop",
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

    /**
     * Returns true if the variable name is backed by a function parameter (directly or via alias).
     * When the target is parameter-backed, we have proof it's a collection being scanned.
     */
    private fun isParamBacked(
        varName: String,
        fn: FunctionDecl,
    ): Boolean =
        fn.parameterFlows.any { flow ->
            flow.paramName == varName ||
                flow.flowsInto.any {
                    it is FlowTarget.MethodCallReceiver && it.methodName == varName
                }
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
        com.github.tvinke.algorilla.model.LoopKind.PARALLEL_STREAM_FOR_EACH -> "parallelStream().forEach()"
        com.github.tvinke.algorilla.model.LoopKind.HIGHER_ORDER -> "forEach()"
    }
