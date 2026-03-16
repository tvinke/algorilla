package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.model.Confidence
import com.github.tvinke.algorilla.model.ExecutionContext
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LookupCall
import com.github.tvinke.algorilla.model.LoopKind
import com.github.tvinke.algorilla.model.LoopNode
import com.github.tvinke.algorilla.model.ObjectCreation
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.model.SourceLocation
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.ComplexityEstimate
import com.github.tvinke.algorilla.rules.ComplexityModel
import com.github.tvinke.algorilla.rules.Evidence
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.Rule
import com.github.tvinke.algorilla.rules.RuleCategory
import com.github.tvinke.algorilla.rules.Suggestion
import com.github.tvinke.algorilla.util.CrossMethodResolver
import com.github.tvinke.algorilla.util.findDescendants

/**
 * Detects expensive operations inside higher-order function callbacks (filter, map, reduce,
 * forEach, removeIf). Catches date parsing/creation, heavyweight object construction,
 * regex compilation, linear lookups, and nested iteration inside callbacks.
 *
 * Handles both LoopNode-based callbacks (forEach) and stream-API patterns where the parser
 * produces LookupCall/FunctionCall nodes (filter, map, reduce, flatMap, peek).
 *
 * Also follows method references via cross-method resolution to catch indirect patterns.
 */
@Suppress("LargeClass", "TooManyFunctions")
public class ExpensiveCallbackRule : Rule {
    override val id: String = "expensive-callback"
    override val name: String = "Expensive Callback"
    override val severity: Severity = Severity.WARNING
    override val languages: Set<Language> = Language.entries.toSet()
    override val category: RuleCategory = RuleCategory.LOOP_AMPLIFIER
    override val defaultConfidence: Confidence = Confidence.LOW
    override val requiresTypeContext: Boolean = true
    override val aliases: List<String> = listOf("date-in-callback")

    override fun evaluate(context: AnalysisContext): List<Finding> {
        val findings = mutableListOf<Finding>()
        for ((_, fileRoot) in context.irTrees) {
            scanNode(fileRoot, null, fileRoot.language, context, findings)
        }
        return findings
    }

    private fun scanNode(
        node: IRNode,
        enclosingFn: FunctionDecl?,
        language: Language,
        context: AnalysisContext,
        findings: MutableList<Finding>,
    ) {
        val fn = if (node is FunctionDecl) node else enclosingFn
        // Skip callbacks inside recursive functions — tree walkers are O(tree_size), not O(n²)
        if (fn == null || !fn.isRecursive) {
            val container = asCallbackContainer(node)
            if (container != null) {
                checkCallbackBody(container, language, context, findings)
            }
        }
        for (child in node.children) {
            scanNode(child, fn, language, context, findings)
        }
    }

    private fun checkCallbackBody(
        container: CallbackContainer,
        language: Language,
        context: AnalysisContext,
        findings: MutableList<Finding>,
    ) {
        val body = container.body
        val creations = body.filterIsInstance<ObjectCreation>() + body.flatMap { it.findDescendants<ObjectCreation>() }
        val calls = body.filterIsInstance<FunctionCall>() + body.flatMap { it.findDescendants<FunctionCall>() }
        val lookups = body.filterIsInstance<LookupCall>() + body.flatMap { it.findDescendants<LookupCall>() }
        val nestedLoops = body.filterIsInstance<LoopNode>() + body.flatMap { it.findDescendants<LoopNode>() }

        for (creation in creations.filter { isDateType(it.typeName, language, context.registry) }) {
            findings.add(buildDateCreationFinding(container, creation))
        }
        for (call in calls.filter { isDateParseCall(it, language, context.registry) }) {
            findings.add(buildDateParseFinding(container, call))
        }
        val regexTypes = context.registry.regexTypes(language)
        for (creation in creations.filter { it.typeName in regexTypes }) {
            findings.add(buildRegexCreationFinding(container, creation))
        }
        for (call in calls.filter { isCompileCall(it) }) {
            findings.add(buildRegexCompileFinding(container, call))
        }
        for (lookup in lookups.filter { !it.isO1 && !it.isScalar }) {
            findings.add(buildLookupFinding(container, lookup))
        }
        for (nested in nestedLoops) {
            findings.add(buildNestedIterationFinding(container, nested))
        }
        checkHeavyweightCreations(container, creations, language, context, findings)
        checkCrossMethod(container, calls, language, context, findings)
    }

    private fun checkHeavyweightCreations(
        container: CallbackContainer,
        creations: List<ObjectCreation>,
        language: Language,
        context: AnalysisContext,
        findings: MutableList<Finding>,
    ) {
        val regexTypesForHeavy = context.registry.regexTypes(language)
        for (creation in creations.filter { !isDateType(it.typeName, language, context.registry) && it.typeName !in regexTypesForHeavy }) {
            val isHeavy = context.registry.isHeavyweight(language, creation.typeName)
            if (isHeavy) {
                findings.add(buildHeavyweightFinding(container, creation))
            }
        }
    }

    private fun checkCrossMethod(
        container: CallbackContainer,
        calls: List<FunctionCall>,
        language: Language,
        context: AnalysisContext,
        findings: MutableList<Finding>,
    ) {
        val maxDepth = context.config.maxCallDepth.coerceAtMost(2)
        for (call in calls.filter { !isDateParseCall(it, language, context.registry) && !isCompileCall(it) }) {
            checkCrossMethodForCall(container, call, language, context, maxDepth, findings)
        }
    }

    private fun checkCrossMethodForCall(
        container: CallbackContainer,
        call: FunctionCall,
        language: Language,
        context: AnalysisContext,
        maxDepth: Int,
        findings: MutableList<Finding>,
    ) {
        val dateOp =
            CrossMethodResolver.resolveAndFind<ObjectCreation>(
                call,
                context.symbolTable,
                maxDepth = maxDepth,
                language = language,
            ) { isDateType(it.typeName, language, context.registry) }
        if (dateOp != null) {
            findings.add(buildIndirectFinding(container, call, dateOp))
            return
        }
        val parseOp =
            CrossMethodResolver.resolveAndFind<FunctionCall>(
                call,
                context.symbolTable,
                maxDepth = maxDepth,
                language = language,
            ) { isDateParseCall(it, language, context.registry) }
        if (parseOp != null) {
            findings.add(buildIndirectFinding(container, call, parseOp))
        }
    }

    // ── Finding builders ────────────────────────────────────────

    private fun buildDateCreationFinding(
        container: CallbackContainer,
        creation: ObjectCreation,
    ): Finding {
        val cx = ComplexityModel.loopTimesCost(container.varName, "parse")
        val inner =
            Evidence(
                creation.location,
                "new ${creation.typeName}() inside callback",
                ExecutionContext.INSIDE_LOOP,
                depth = 1,
                complexity = ComplexityModel.bottleneck("parse"),
            )
        return makeFinding(container, creation.location, cx, inner, "${creation.typeName} creation inside callback")
    }

    private fun buildDateParseFinding(
        container: CallbackContainer,
        call: FunctionCall,
    ): Finding {
        val cx = ComplexityModel.loopTimesCost(container.varName, "parse")
        val target = call.qualifiedTarget ?: "Date"
        val inner =
            Evidence(
                call.location,
                "$target.${call.name}() inside callback",
                ExecutionContext.INSIDE_LOOP,
                depth = 1,
                complexity = ComplexityModel.bottleneck("parse"),
            )
        return makeFinding(container, call.location, cx, inner, "Date parsing (${call.name}) inside callback")
    }

    private fun buildRegexCreationFinding(
        container: CallbackContainer,
        creation: ObjectCreation,
    ): Finding {
        val cx = ComplexityModel.loopTimesCost(container.varName, "compile")
        val inner =
            Evidence(
                creation.location,
                "new ${creation.typeName}() inside callback",
                ExecutionContext.INSIDE_LOOP,
                depth = 1,
                complexity = ComplexityModel.bottleneck("compile"),
            )
        return makeFinding(
            container,
            creation.location,
            cx,
            inner,
            "Regex compilation (new ${creation.typeName}()) inside callback",
        )
    }

    private fun buildRegexCompileFinding(
        container: CallbackContainer,
        call: FunctionCall,
    ): Finding {
        val cx = ComplexityModel.loopTimesCost(container.varName, "compile")
        val target = call.qualifiedTarget ?: "Pattern"
        val inner =
            Evidence(
                call.location,
                "$target.${call.name}() inside callback",
                ExecutionContext.INSIDE_LOOP,
                depth = 1,
                complexity = ComplexityModel.bottleneck("compile"),
            )
        return makeFinding(
            container,
            call.location,
            cx,
            inner,
            "Regex compilation ($target.${call.name}()) inside callback",
        )
    }

    private fun buildLookupFinding(
        container: CallbackContainer,
        lookup: LookupCall,
    ): Finding {
        val targetVar = lookup.targetVariable ?: "collection"
        val cx = ComplexityModel.loopTimesLookup(container.varName, targetVar)
        val inner =
            Evidence(
                lookup.location,
                "linear ${lookup.kind.label} on '$targetVar' inside callback",
                ExecutionContext.INSIDE_LOOP,
                depth = 1,
                complexity = ComplexityModel.bottleneckO(targetVar),
            )
        return Finding(
            ruleId = id,
            ruleName = name,
            severity = severity,
            location = lookup.location,
            message = "Linear ${lookup.kind.label} on '$targetVar' inside callback",
            suggestions = listOf(Suggestion.Freeform("Build a HashSet/Map from '$targetVar' before the loop")),
            currentComplexity = cx.current,
            suggestedComplexity = cx.suggested,
            evidence = listOf(container.evidence(), inner),
        )
    }

    private fun buildNestedIterationFinding(
        container: CallbackContainer,
        nested: LoopNode,
    ): Finding {
        val nestedVar = nested.iteratedVariable ?: "inner"
        val cx = ComplexityModel.loopTimesLookup(container.varName, nestedVar)
        val inner =
            Evidence(
                nested.location,
                "nested iteration over $nestedVar inside callback",
                ExecutionContext.INSIDE_LOOP,
                depth = 1,
                complexity = ComplexityModel.bottleneckO(nestedVar),
            )
        return Finding(
            ruleId = id,
            ruleName = name,
            severity = severity,
            location = nested.location,
            message = "Nested iteration over '$nestedVar' inside callback",
            suggestions =
                listOf(
                    Suggestion.Freeform("Pre-build a Set/Map from '$nestedVar' before the outer loop, or restructure to avoid O(n\u00b2)"),
                ),
            currentComplexity = cx.current,
            suggestedComplexity = cx.suggested,
            evidence = listOf(container.evidence(), inner),
        )
    }

    private fun buildHeavyweightFinding(
        container: CallbackContainer,
        creation: ObjectCreation,
    ): Finding {
        val cx = ComplexityModel.loopTimesCost(container.varName, "init")
        val inner =
            Evidence(
                creation.location,
                "new ${creation.typeName}() inside callback",
                ExecutionContext.INSIDE_LOOP,
                depth = 1,
                complexity = ComplexityModel.bottleneck("init"),
            )
        return makeFinding(container, creation.location, cx, inner, "Heavyweight ${creation.typeName} creation inside callback")
    }

    private fun buildIndirectFinding(
        container: CallbackContainer,
        call: FunctionCall,
        innerNode: IRNode,
    ): Finding {
        val cx = ComplexityModel.loopTimesCost(container.varName, "parse")
        val desc = describeNode(innerNode)
        val evidence =
            listOf(
                container.evidence(),
                Evidence(call.location, "${call.name}() called from callback", ExecutionContext.INSIDE_LOOP, depth = 1),
                Evidence(
                    innerNode.location,
                    "$desc inside ${call.name}()",
                    ExecutionContext.INSIDE_LOOP,
                    depth = 2,
                    complexity = ComplexityModel.bottleneck("parse"),
                ),
            )
        return Finding(
            ruleId = id,
            ruleName = name,
            severity = severity,
            location = innerNode.location,
            message = "Expensive operation inside ${call.name}() called from callback",
            suggestions = listOf(Suggestion.Freeform("Hoist expensive operation before the loop, or pre-compute values into a Map")),
            currentComplexity = cx.current,
            suggestedComplexity = cx.suggested,
            evidence = evidence,
        )
    }

    private fun makeFinding(
        container: CallbackContainer,
        location: SourceLocation,
        cx: ComplexityEstimate,
        innerEvidence: Evidence,
        message: String,
    ) = Finding(
        ruleId = id,
        ruleName = name,
        severity = severity,
        location = location,
        message = message,
        suggestions = listOf(Suggestion.Freeform("Hoist expensive operation before the loop, or pre-compute values into a Map")),
        currentComplexity = cx.current,
        suggestedComplexity = cx.suggested,
        evidence = listOf(container.evidence(), innerEvidence),
    )

    private fun describeNode(node: IRNode): String =
        when (node) {
            is ObjectCreation -> "new ${node.typeName}()"
            is FunctionCall -> "${node.qualifiedTarget ?: ""}.${node.name}()"
            else -> "expensive operation"
        }
}

// ── Callback container abstraction ──────────────────────────────

/**
 * Unifies LoopNode (forEach), LookupCall (filter/find), and HOF FunctionCall (map/reduce)
 * as callback containers whose children execute per-element.
 */
private data class CallbackContainer(
    val location: SourceLocation,
    val varName: String,
    val label: String,
    val body: List<IRNode>,
) {
    fun evidence(): Evidence =
        Evidence(
            location,
            label,
            ExecutionContext.SINGLE,
            complexity = ComplexityModel.loopEvidence(varName),
        )
}

/** Higher-order function names that take a callback (stream API methods besides forEach). */
private val HOF_METHODS = setOf("map", "flatMap", "reduce", "peek", "collect")

private fun asCallbackContainer(node: IRNode): CallbackContainer? =
    when {
        node is LoopNode && (node.kind == LoopKind.HIGHER_ORDER || node.kind == LoopKind.STREAM_FOR_EACH) -> {
            val varName = node.iteratedVariable ?: "items"
            CallbackContainer(node.location, varName, "forEach/map/filter over $varName", node.children)
        }
        node is LookupCall && !node.isO1 -> {
            val varName = node.targetVariable ?: "items"
            CallbackContainer(node.location, varName, "${node.kind.label}() over $varName", node.children)
        }
        node is FunctionCall && node.name in HOF_METHODS -> {
            val varName = node.qualifiedTarget ?: "items"
            CallbackContainer(node.location, varName, "${node.name}() over $varName", node.children)
        }
        else -> null
    }

private fun isCompileCall(call: FunctionCall): Boolean =
    call.name == "compile" &&
        (
            call.qualifiedTarget?.contains("Pattern") == true ||
                call.qualifiedTarget?.contains("Regex") == true
        )
