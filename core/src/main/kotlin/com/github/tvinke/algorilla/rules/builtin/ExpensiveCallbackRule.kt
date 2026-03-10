package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.model.ExecutionContext
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LoopKind
import com.github.tvinke.algorilla.model.LoopNode
import com.github.tvinke.algorilla.model.ObjectCreation
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.ComplexityModel
import com.github.tvinke.algorilla.rules.Evidence
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.Rule
import com.github.tvinke.algorilla.rules.RuleCategory
import com.github.tvinke.algorilla.util.CrossMethodResolver
import com.github.tvinke.algorilla.util.findDescendants

/**
 * Detects expensive operations inside higher-order function callbacks (filter, map, reduce,
 * forEach, removeIf). When a callback performs date parsing, date creation, or heavyweight
 * object construction, every iteration pays that cost unnecessarily.
 *
 * Also follows method references via cross-method resolution to catch indirect patterns.
 */
@Suppress("LargeClass")
public class ExpensiveCallbackRule : Rule {
    override val id: String = "expensive-callback"
    override val name: String = "Expensive Callback"
    override val severity: Severity = Severity.WARNING
    override val languages: Set<Language> = Language.entries.toSet()
    override val category: RuleCategory = RuleCategory.LOOP_AMPLIFIER

    override fun evaluate(context: AnalysisContext): List<Finding> {
        val findings = mutableListOf<Finding>()
        for ((_, fileRoot) in context.irTrees) {
            scanNode(fileRoot, context, findings)
        }
        return findings
    }

    private fun scanNode(
        node: IRNode,
        context: AnalysisContext,
        findings: MutableList<Finding>,
    ) {
        if (node is LoopNode && (node.kind == LoopKind.HIGHER_ORDER || node.kind == LoopKind.STREAM_FOR_EACH)) {
            checkCallbackBody(node, context, findings)
        }
        for (child in node.children) {
            scanNode(child, context, findings)
        }
    }

    private fun checkCallbackBody(
        loop: LoopNode,
        context: AnalysisContext,
        findings: MutableList<Finding>,
    ) {
        val body = loop.children
        val creations = body.filterIsInstance<ObjectCreation>() + body.flatMap { it.findDescendants<ObjectCreation>() }
        val calls = body.filterIsInstance<FunctionCall>() + body.flatMap { it.findDescendants<FunctionCall>() }

        for (creation in creations.filter { isDateType(it.typeName) }) {
            findings.add(buildDateCreationFinding(loop, creation))
        }
        for (call in calls.filter { isDateParseCall(it) }) {
            findings.add(buildDateParseFinding(loop, call))
        }
        checkHeavyweightCreations(loop, creations, context, findings)
        checkCrossMethod(loop, calls, context, findings)
    }

    private fun checkHeavyweightCreations(
        loop: LoopNode,
        creations: List<ObjectCreation>,
        context: AnalysisContext,
        findings: MutableList<Finding>,
    ) {
        for (creation in creations) {
            if (isDateType(creation.typeName)) continue
            val isHeavy =
                context.registry.isHeavyweight(Language.JAVA, creation.typeName) ||
                    context.registry.allHeavyweightTypes().any { creation.typeName.contains(it) }
            if (isHeavy) {
                findings.add(buildHeavyweightFinding(loop, creation))
            }
        }
    }

    private fun checkCrossMethod(
        loop: LoopNode,
        calls: List<FunctionCall>,
        context: AnalysisContext,
        findings: MutableList<Finding>,
    ) {
        val maxDepth = context.config.maxCallDepth.coerceAtMost(2)
        for (call in calls.filter { !isDateParseCall(it) }) {
            checkCrossMethodForCall(loop, call, context, maxDepth, findings)
        }
    }

    private fun checkCrossMethodForCall(
        loop: LoopNode,
        call: FunctionCall,
        context: AnalysisContext,
        maxDepth: Int,
        findings: MutableList<Finding>,
    ) {
        val dateOp =
            CrossMethodResolver.resolveAndFind<ObjectCreation>(
                call,
                context.symbolTable,
                maxDepth = maxDepth,
            ) { isDateType(it.typeName) }
        if (dateOp != null) {
            findings.add(buildIndirectFinding(loop, call, dateOp))
            return
        }
        val parseOp =
            CrossMethodResolver.resolveAndFind<FunctionCall>(
                call,
                context.symbolTable,
                maxDepth = maxDepth,
            ) { isDateParseCall(it) }
        if (parseOp != null) {
            findings.add(buildIndirectFinding(loop, call, parseOp))
        }
    }

    private fun loopEvidence(loop: LoopNode): Evidence {
        val varName = loop.iteratedVariable ?: "items"
        return Evidence(
            loop.location,
            "forEach/map/filter over $varName",
            ExecutionContext.SINGLE,
            complexity = ComplexityModel.loopEvidence(varName),
        )
    }

    private fun buildDateCreationFinding(
        loop: LoopNode,
        creation: ObjectCreation,
    ): Finding {
        val varName = loop.iteratedVariable ?: "items"
        val cx = ComplexityModel.loopTimesCost(varName, "parse")
        val innerEvidence =
            Evidence(
                creation.location,
                "new ${creation.typeName}() inside callback",
                ExecutionContext.INSIDE_LOOP,
                depth = 1,
                complexity = ComplexityModel.bottleneck("parse"),
            )
        return buildFinding(loop, creation.location, cx, innerEvidence, "${creation.typeName} creation inside callback")
    }

    private fun buildDateParseFinding(
        loop: LoopNode,
        call: FunctionCall,
    ): Finding {
        val varName = loop.iteratedVariable ?: "items"
        val cx = ComplexityModel.loopTimesCost(varName, "parse")
        val target = call.qualifiedTarget ?: "Date"
        val innerEvidence =
            Evidence(
                call.location,
                "$target.${call.name}() inside callback",
                ExecutionContext.INSIDE_LOOP,
                depth = 1,
                complexity = ComplexityModel.bottleneck("parse"),
            )
        return buildFinding(loop, call.location, cx, innerEvidence, "Date parsing (${call.name}) inside callback")
    }

    private fun buildHeavyweightFinding(
        loop: LoopNode,
        creation: ObjectCreation,
    ): Finding {
        val varName = loop.iteratedVariable ?: "items"
        val cx = ComplexityModel.loopTimesCost(varName, "init")
        val innerEvidence =
            Evidence(
                creation.location,
                "new ${creation.typeName}() inside callback",
                ExecutionContext.INSIDE_LOOP,
                depth = 1,
                complexity = ComplexityModel.bottleneck("init"),
            )
        return buildFinding(
            loop,
            creation.location,
            cx,
            innerEvidence,
            "Heavyweight ${creation.typeName} creation inside callback",
        )
    }

    private fun buildIndirectFinding(
        loop: LoopNode,
        call: FunctionCall,
        innerNode: IRNode,
    ): Finding {
        val varName = loop.iteratedVariable ?: "items"
        val cx = ComplexityModel.loopTimesCost(varName, "parse")
        val desc = describeNode(innerNode)
        val evidence =
            listOf(
                loopEvidence(loop),
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
            suggestion = "Hoist expensive operation before the loop, or pre-compute values into a Map",
            currentComplexity = cx.current,
            suggestedComplexity = cx.suggested,
            evidence = evidence,
        )
    }

    private fun describeNode(node: IRNode): String =
        when (node) {
            is ObjectCreation -> "new ${node.typeName}()"
            is FunctionCall -> "${node.qualifiedTarget ?: ""}.${node.name}()"
            else -> "expensive operation"
        }

    private fun buildFinding(
        loop: LoopNode,
        location: com.github.tvinke.algorilla.model.SourceLocation,
        cx: com.github.tvinke.algorilla.rules.ComplexityEstimate,
        innerEvidence: Evidence,
        message: String,
    ) = Finding(
        ruleId = id,
        ruleName = name,
        severity = severity,
        location = location,
        message = message,
        suggestion = "Hoist expensive operation before the loop, or pre-compute values into a Map",
        currentComplexity = cx.current,
        suggestedComplexity = cx.suggested,
        evidence = listOf(loopEvidence(loop), innerEvidence),
    )
}
