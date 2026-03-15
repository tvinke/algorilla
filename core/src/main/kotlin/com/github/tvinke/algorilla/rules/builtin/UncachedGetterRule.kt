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
import com.github.tvinke.algorilla.semantics.LanguageSemanticsRegistry
import com.github.tvinke.algorilla.util.findDescendantsWithBranchContext
import com.github.tvinke.algorilla.util.maxCoExecutableSubset

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
        val getterCalls = callsWithContext.filter { isGetterPattern(it.first) && it.first.arguments.isNotEmpty() }
        val grouped = getterCalls.groupBy { "${it.first.qualifiedTarget}.${it.first.name}(${argKey(it.first)})" }

        for ((_, duplicatesWithContext) in grouped) {
            val coExecutable = maxCoExecutableSubset(duplicatesWithContext)
            if (coExecutable.size >= MIN_CALLS) {
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
        val cx = ComplexityModel.uncachedGetter(calls.size)
        return Finding(
            ruleId = id,
            ruleName = name,
            severity = severity,
            location = first.location,
            message = "$callDesc called ${calls.size} times with same argument in ${fn.name}()",
            suggestion = "Cache the result in a local variable: val x = $callDesc",
            currentComplexity = cx.current,
            suggestedComplexity = cx.suggested,
            evidence = evidence,
        )
    }

    internal companion object {
        const val MIN_CALLS = 2
    }
}

private val GETTER_PREFIXES: List<String> by lazy {
    LanguageSemanticsRegistry.DEFAULT
        .allGetterPrefixes()
}

/** Method names excluded from getter detection. YAML-driven via `getter-excluded-names`. */
private val EXCLUDED_NAMES: Set<String> by lazy {
    LanguageSemanticsRegistry.DEFAULT.allExtraSection("getter-excluded-names")
}

private fun isGetterPattern(call: FunctionCall): Boolean {
    if (call.name in EXCLUDED_NAMES) return false
    return GETTER_PREFIXES.any { call.name.startsWith(it, ignoreCase = true) }
}

private fun argKey(call: FunctionCall): String =
    call.arguments.joinToString(",") { arg ->
        when (arg) {
            is FunctionCall -> "${arg.qualifiedTarget}.${arg.name}/${arg.arguments.size}"
            is GenericNode -> arg.nodeType
            else -> "${arg::class.simpleName}@${arg.location.line}:${arg.location.column}"
        }
    }
