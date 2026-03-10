package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.model.ExecutionContext
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.GenericNode
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.ComplexityModel
import com.github.tvinke.algorilla.rules.Evidence
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.Rule
import com.github.tvinke.algorilla.rules.RuleCategory
import com.github.tvinke.algorilla.semantics.CollectionSemanticsRegistry
import com.github.tvinke.algorilla.semantics.MethodPurity
import com.github.tvinke.algorilla.util.findDescendantsWithBranchContext
import com.github.tvinke.algorilla.util.maxCoExecutableSubset

/**
 * Detects the same parameterized call invoked multiple times with the same arguments
 * within a single function. The result should be cached in a local variable.
 */
public class RedundantExpensiveCallRule : Rule {
    override val id: String = "redundant-expensive-call"
    override val name: String = "Redundant Expensive Call"
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
        val callsWithContext = fn.findDescendantsWithBranchContext<FunctionCall>()
        val filtered = callsWithContext.filter { it.first.arguments.isNotEmpty() && !isSideEffectCall(it.first) }
        val grouped = filtered.groupBy { callSignature(it.first) }

        for ((sig, duplicatesWithContext) in grouped) {
            if (sig.isBlank()) continue
            val coExecutable = maxCoExecutableSubset(duplicatesWithContext)
            if (coExecutable.size >= MIN_DUPLICATES) {
                findings.add(buildFinding(fn, coExecutable))
            }
        }
    }

    private fun buildFinding(
        fn: FunctionDecl,
        calls: List<FunctionCall>,
    ): Finding {
        val first = calls.first()
        val callDesc = "${first.qualifiedTarget ?: ""}${if (first.qualifiedTarget != null) "." else ""}${first.name}()"
        val evidence =
            calls.mapIndexed { idx, call ->
                val tag = if (idx == 0) "1st call" else "duplicate"
                Evidence(call.location, "$callDesc ($tag)", ExecutionContext.SINGLE)
            }
        val cx = ComplexityModel.redundantCalls(calls.size)
        return Finding(
            ruleId = id,
            ruleName = name,
            severity = severity,
            location = first.location,
            message = "$callDesc called ${calls.size} times with same arguments in ${fn.name}()",
            suggestion = "Cache the result in a local variable",
            currentComplexity = cx.current,
            suggestedComplexity = cx.suggested,
            evidence = evidence,
        )
    }

    internal companion object {
        const val MIN_DUPLICATES = 2
    }
}

/**
 * Creates a signature string from a call's name + argument text for grouping.
 * Uses the children's toString as a rough equality check.
 */
private fun callSignature(call: FunctionCall): String {
    val target = call.qualifiedTarget ?: ""
    val argsKey = call.arguments.joinToString(",") { argFingerprint(it) }
    return "$target.${call.name}($argsKey)"
}

private fun argFingerprint(node: IRNode): String =
    when (node) {
        is FunctionCall -> {
            val base = "${node.qualifiedTarget}.${node.name}(${node.arguments.joinToString(",") { argFingerprint(it) }})"
            if (containsNonDeterministic(node)) "$base@${node.location.line}" else base
        }
        is GenericNode -> node.nodeType
        else -> "${node::class.simpleName}@${node.location.line}:${node.location.column}"
    }

/** Methods whose return value differs on each call — prevents grouping calls with these as arguments. */
private val NON_DETERMINISTIC = setOf("randomUUID", "random", "now", "currentTimeMillis", "nanoTime")

private fun containsNonDeterministic(call: FunctionCall): Boolean {
    if (call.name in NON_DETERMINISTIC) return true
    return call.children.any { it is FunctionCall && containsNonDeterministic(it) }
}

/**
 * Trivial and builder methods derived from the semantics registry (YAML).
 * To add methods, update the trivial-methods or builder-methods section
 * in the language YAML files under core/src/main/resources/semantics/.
 */
private val TRIVIAL_METHODS: Set<String> by lazy {
    CollectionSemanticsRegistry.loadDefaults().allTrivialMethods()
}

private val BUILDER_METHODS: Set<String> by lazy {
    CollectionSemanticsRegistry.loadDefaults().allBuilderMethods()
}

/**
 * Known constant-time operations that are too cheap to flag even when duplicated.
 * Includes Java time/date arithmetic, enum accessors, and instant conversions.
 */
private val CHEAP_METHODS =
    setOf(
        // Java time/date arithmetic
        "plusDays",
        "minusDays",
        "plusHours",
        "minusHours",
        "plusMinutes",
        "minusMinutes",
        "plusSeconds",
        "minusSeconds",
        "plusWeeks",
        "minusWeeks",
        "plusMonths",
        "minusMonths",
        "ofHours",
        "ofMinutes",
        "ofSeconds",
        "ofMillis",
        "ofNanos",
        "ofDays",
        "toInstant",
        "atStartOfDay",
        "atZone",
        // Reflection / modifier checks
        "isStatic",
        "isPublic",
        "isPrivate",
        "isProtected",
        "isAbstract",
        "isFinal",
        "isInterface",
        "isSynthetic",
        "isAnnotationPresent",
        // String conversions and lookups
        "startsWith",
        "endsWith",
        "substring",
        "replace",
        "replaceAll",
        "replaceFirst",
        "split",
        "matches",
        "toLowerCase",
        "toUpperCase",
        "trim",
        "strip",
        "charAt",
        "indexOf",
        "lastIndexOf",
        "contains",
        "isEmpty",
        "isBlank",
        "length",
        "concat",
        "valueOf",
    )

private fun isSideEffectCall(call: FunctionCall): Boolean {
    if (call.name in TRIVIAL_METHODS) return true
    if (call.name in BUILDER_METHODS) return true
    if (call.name in CHEAP_METHODS) return true
    return MethodPurity.isSideEffect(call.name, call.qualifiedTarget)
}
