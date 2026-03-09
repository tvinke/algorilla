package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.model.ExecutionContext
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Evidence
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.Rule
import com.github.tvinke.algorilla.semantics.MethodPurity
import com.github.tvinke.algorilla.util.findDescendants

/**
 * Detects the same parameterized call invoked multiple times with the same arguments
 * within a single function. The result should be cached in a local variable.
 */
public class RedundantExpensiveCallRule : Rule {
    override val id: String = "redundant-expensive-call"
    override val name: String = "Redundant Expensive Call"
    override val severity: Severity = Severity.INFO
    override val languages: Set<Language> = Language.entries.toSet()

    override fun evaluate(context: AnalysisContext): List<Finding> {
        val findings = mutableListOf<Finding>()
        for ((_, fileRoot) in context.irTrees) {
            scanNode(fileRoot, findings)
        }
        return findings
    }

    private fun scanNode(node: IRNode, findings: MutableList<Finding>) {
        if (node is FunctionDecl) {
            checkFunction(node, findings)
        }
        for (child in node.children) {
            scanNode(child, findings)
        }
    }

    private fun checkFunction(fn: FunctionDecl, findings: MutableList<Finding>) {
        val calls = fn.findDescendants<FunctionCall>()
        val grouped = calls
            .filter { it.arguments.isNotEmpty() && !isSideEffectCall(it) }
            .groupBy { callSignature(it) }

        for ((sig, duplicates) in grouped) {
            if (duplicates.size >= MIN_DUPLICATES && sig.isNotBlank()) {
                findings.add(buildFinding(fn, duplicates))
            }
        }
    }

    private fun buildFinding(fn: FunctionDecl, calls: List<FunctionCall>): Finding {
        val first = calls.first()
        val callDesc = "${first.qualifiedTarget ?: ""}${if (first.qualifiedTarget != null) "." else ""}${first.name}()"
        val evidence = calls.map { call ->
            Evidence(call.location, "$callDesc (duplicate)", ExecutionContext.SINGLE)
        }
        return Finding(
            ruleId = id, ruleName = name, severity = severity,
            location = first.location,
            message = "$callDesc called ${calls.size} times with same arguments in ${fn.name}()",
            suggestion = "Cache the result in a local variable",
            currentComplexity = "O(${calls.size}x)", suggestedComplexity = "O(1x)",
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
        is FunctionCall -> "${node.qualifiedTarget}.${node.name}"
        else -> node.toString().take(100)
    }

/** Trivially cheap methods that are never worth caching */
private val TRIVIAL_METHODS = setOf(
    "equals", "hashCode", "toString", "valueOf", "compareTo",
    "isDigit", "isLetter", "isUpperCase", "isLowerCase", "isWhitespace",
    "toIntExact", "abs", "min", "max", "ceil", "floor", "round",
    "isEmpty", "isBlank", "isPresent", "isNull", "isNotNull",
    "size", "length", "trim", "lowercase", "uppercase",
    "charAt", "codePointAt", "getClass", "name", "ordinal",
)

/**
 * Builder-style method names that are called for chaining rather than return value.
 * These overlap with pure prefixes but are side-effectful in builder context.
 */
private val BUILDER_METHODS = setOf(
    "path", "header", "param", "query", "body", "accept", "contentType",
)

private fun isSideEffectCall(call: FunctionCall): Boolean {
    if (call.name in TRIVIAL_METHODS) return true
    if (call.name in BUILDER_METHODS) return true
    return MethodPurity.isSideEffect(call.name, call.qualifiedTarget)
}
