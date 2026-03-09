package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.model.ExecutionContext
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LookupCall
import com.github.tvinke.algorilla.model.LoopNode
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.model.VariableDecl
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Evidence
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.Rule
import com.github.tvinke.algorilla.rules.RuleCategory
import com.github.tvinke.algorilla.util.CrossMethodResolver
import com.github.tvinke.algorilla.util.findDescendants

/**
 * Detects cascading getter patterns where the result of one lookup feeds into another:
 * `a = getX(id); b = getY(a); c = getZ(b)` — if each getter is O(n), the chain
 * multiplies to O(n^k). Suggests pre-building lookup maps or using joins.
 */
public class ChainedGettersRule : Rule {
    override val id: String = "chained-getters"
    override val name: String = "Chained Getters"
    override val severity: Severity = Severity.INFO
    override val languages: Set<Language> = Language.entries.toSet()
    override val category: RuleCategory = RuleCategory.REDUNDANCY

    override fun evaluate(context: AnalysisContext): List<Finding> {
        val findings = mutableListOf<Finding>()
        for ((_, fileRoot) in context.irTrees) {
            for (fn in fileRoot.findDescendants<FunctionDecl>()) {
                checkFunction(fn, context, findings)
            }
        }
        return findings
    }

    private fun checkFunction(fn: FunctionDecl, context: AnalysisContext, findings: MutableList<Finding>) {
        val varDecls = fn.findDescendants<VariableDecl>()
        val calls = fn.findDescendants<FunctionCall>()

        // Build a map: variable name -> the getter call that produced it
        val producedBy = mutableMapOf<String, FunctionCall>()
        for (decl in varDecls) {
            val initCall = decl.children.filterIsInstance<FunctionCall>().firstOrNull()
            if (initCall != null && isGetterPattern(initCall)) {
                producedBy[decl.name] = initCall
            }
        }

        // Find chains: a getter call whose argument references a variable produced by another getter
        for (call in calls) {
            if (!isGetterPattern(call)) continue
            val chain = buildChain(call, producedBy)
            if (chain.size >= MIN_CHAIN_LENGTH) {
                // Check if any getter in the chain resolves to a function with linear lookups
                val hasLinear = chain.any { c ->
                    val resolved = CrossMethodResolver.resolve(c, context.symbolTable)
                    resolved != null && (resolved.findDescendants<LookupCall>().isNotEmpty() ||
                        resolved.findDescendants<LoopNode>().isNotEmpty())
                }
                if (hasLinear) {
                    findings.add(buildFinding(fn, chain))
                }
            }
        }
    }

    private fun buildChain(
        call: FunctionCall,
        producedBy: Map<String, FunctionCall>,
        visited: Set<String> = emptySet(),
    ): List<FunctionCall> {
        val chain = mutableListOf(call)
        // Walk backward through the arg -> produced-by chain
        for (arg in call.arguments) {
            val argName = simpleVarName(arg) ?: continue
            if (argName in visited) continue
            val producer = producedBy[argName] ?: continue
            chain.addAll(0, buildChain(producer, producedBy, visited + argName))
        }
        return chain
    }

    private fun buildFinding(fn: FunctionDecl, chain: List<FunctionCall>): Finding {
        val evidence = chain.mapIndexed { idx, call ->
            val label = if (idx == 0) "${call.name}() starts the chain" else "${call.name}() uses result of previous"
            Evidence(call.location, label, ExecutionContext.SINGLE)
        }
        val chainDesc = chain.joinToString(" → ") { it.name }
        return Finding(
            ruleId = id, ruleName = name, severity = severity,
            location = chain.first().location,
            message = "Chained getter cascade in ${fn.name}(): $chainDesc",
            suggestion = "Pre-build lookup maps or use a join query to avoid cascading lookups",
            currentComplexity = "O(n^${chain.size})", suggestedComplexity = "O(n)",
            evidence = evidence,
        )
    }

    internal companion object {
        const val MIN_CHAIN_LENGTH = 2
    }
}

private val GETTER_PREFIXES = listOf("get", "find", "load", "fetch", "lookup", "resolve")

private fun isGetterPattern(call: FunctionCall): Boolean =
    GETTER_PREFIXES.any { call.name.startsWith(it, ignoreCase = true) }

private fun simpleVarName(node: IRNode): String? =
    when (node) {
        is FunctionCall -> null
        else -> {
            val text = node.toString()
            if (text.length < 60 && text.matches(Regex("[a-zA-Z_][a-zA-Z0-9_]*"))) text else null
        }
    }
