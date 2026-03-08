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
import com.github.tvinke.algorilla.util.findDescendants

/**
 * Detects the same getter-style call invoked multiple times with the same argument
 * in a function. Getter calls that take an ID parameter and return an entity
 * should be cached in a local variable when called repeatedly.
 */
public class UncachedGetterRule : Rule {
    override val id: String = "uncached-getter"
    override val name: String = "Uncached Getter"
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
        val getterCalls = calls.filter { isGetterPattern(it) && it.arguments.isNotEmpty() }
        val grouped = getterCalls.groupBy { "${it.qualifiedTarget}.${it.name}(${argKey(it)})" }

        for ((_, duplicates) in grouped) {
            if (duplicates.size >= MIN_CALLS) {
                findings.add(buildFinding(fn, duplicates))
            }
        }
    }

    private fun buildFinding(fn: FunctionDecl, calls: List<FunctionCall>): Finding {
        val first = calls.first()
        val callDesc = "${first.qualifiedTarget ?: ""}${if (first.qualifiedTarget != null) "." else ""}${first.name}()"
        val evidence = calls.map {
            Evidence(it.location, "$callDesc called again with same arg", ExecutionContext.SINGLE)
        }
        return Finding(
            ruleId = id, ruleName = name, severity = severity,
            location = first.location,
            message = "$callDesc called ${calls.size} times with same argument in ${fn.name}()",
            suggestion = "Cache the result in a local variable: val x = $callDesc",
            currentComplexity = "${calls.size}x lookup", suggestedComplexity = "1x lookup",
            evidence = evidence,
        )
    }

    internal companion object {
        const val MIN_CALLS = 2
    }
}

private val GETTER_PREFIXES = listOf("get", "find", "load", "fetch", "lookup", "resolve")

private fun isGetterPattern(call: FunctionCall): Boolean =
    GETTER_PREFIXES.any { call.name.startsWith(it, ignoreCase = true) }

private fun argKey(call: FunctionCall): String =
    call.arguments.joinToString(",") { it.toString().take(80) }
