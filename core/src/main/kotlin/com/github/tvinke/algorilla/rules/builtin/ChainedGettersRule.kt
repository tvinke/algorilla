package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.model.Confidence
import com.github.tvinke.algorilla.model.ExecutionContext
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.GenericNode
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LookupCall
import com.github.tvinke.algorilla.model.LoopNode
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.model.VariableDecl
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.ComplexityModel
import com.github.tvinke.algorilla.rules.Evidence
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.Rule
import com.github.tvinke.algorilla.rules.RuleCategory
import com.github.tvinke.algorilla.rules.Suggestion
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
    override val defaultConfidence: Confidence = Confidence.LOW
    override val requiresTypeContext: Boolean = true

    override fun evaluate(context: AnalysisContext): List<Finding> {
        val findings = mutableListOf<Finding>()
        for ((_, fileRoot) in context.irTrees) {
            val prefixes = context.registry.getterPrefixes(fileRoot.language)
            for (fn in fileRoot.findDescendants<FunctionDecl>()) {
                checkFunction(fn, prefixes, fileRoot.language, context, findings)
            }
        }
        return findings
    }

    private fun checkFunction(
        fn: FunctionDecl,
        getterPrefixes: List<String>,
        language: Language,
        context: AnalysisContext,
        findings: MutableList<Finding>,
    ) {
        val varDecls = fn.findDescendants<VariableDecl>()
        val calls = fn.findDescendants<FunctionCall>()

        // Build a map: variable name -> the getter call that produced it
        val producedBy = mutableMapOf<String, FunctionCall>()
        for (decl in varDecls) {
            val initCall = decl.children.filterIsInstance<FunctionCall>().firstOrNull()
            if (initCall != null && isGetterPattern(initCall, getterPrefixes)) {
                producedBy[decl.name] = initCall
            }
        }

        // Find chains: a getter call whose argument references a variable produced by another getter
        for (call in calls) {
            if (!isGetterPattern(call, getterPrefixes)) continue
            val chain = buildChain(call, producedBy)
            if (chain.size >= MIN_CHAIN_LENGTH) {
                // Check if any getter in the chain resolves to a function with linear lookups
                val hasLinear =
                    chain.any { c ->
                        val resolved = CrossMethodResolver.resolve(c, context.symbolTable, language)
                        resolved != null &&
                            (
                                resolved.findDescendants<LookupCall>().isNotEmpty() ||
                                    resolved.findDescendants<LoopNode>().isNotEmpty()
                            )
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
        @Suppress("LoopWithTooManyJumpStatements")
        for (arg in call.arguments) {
            val argName = simpleVarName(arg) ?: continue
            if (argName in visited) continue
            val producer = producedBy[argName] ?: continue
            chain.addAll(0, buildChain(producer, producedBy, visited + argName))
        }
        return chain
    }

    private fun buildFinding(
        fn: FunctionDecl,
        chain: List<FunctionCall>,
    ): Finding {
        val evidence =
            chain.mapIndexed { idx, call ->
                val label = if (idx == 0) "${call.name}() starts the chain" else "${call.name}() uses result of previous"
                Evidence(call.location, label, ExecutionContext.SINGLE, depth = idx, complexity = "IO/query")
            }
        val cx = ComplexityModel.chainedGetters(chain.size)
        val chainDesc = chain.joinToString(" → ") { it.name }
        return Finding(
            ruleId = id,
            ruleName = name,
            severity = severity,
            location = chain.first().location,
            message = "Chained getter cascade in ${fn.name}(): $chainDesc",
            suggestions = listOf(Suggestion.Freeform("Pre-build lookup maps or use a join query to avoid cascading lookups")),
            currentComplexity = cx.current,
            suggestedComplexity = cx.suggested,
            evidence = evidence,
        )
    }

    internal companion object {
        const val MIN_CHAIN_LENGTH = 2
    }
}

private fun isGetterPattern(
    call: FunctionCall,
    getterPrefixes: List<String>,
): Boolean = getterPrefixes.any { call.name.startsWith(it, ignoreCase = true) }

private const val MAX_VAR_NAME_LENGTH = 60

private fun simpleVarName(node: IRNode): String? =
    when (node) {
        is FunctionCall -> null
        is GenericNode -> {
            val text = node.nodeType
            if (text.length < MAX_VAR_NAME_LENGTH && text.matches(Regex("[a-zA-Z_][a-zA-Z0-9_]*"))) text else null
        }
        else -> {
            val text = node.toString()
            if (text.length < MAX_VAR_NAME_LENGTH && text.matches(Regex("[a-zA-Z_][a-zA-Z0-9_]*"))) text else null
        }
    }
