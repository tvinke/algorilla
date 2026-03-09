package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.model.ExecutionContext
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LoopNode
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Evidence
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.Rule
import com.github.tvinke.algorilla.rules.RuleCategory
import com.github.tvinke.algorilla.util.CrossMethodResolver

/**
 * Detects repository/DAO single-record fetch calls inside loops (N+1 problem).
 * Loading records one-by-one in a loop is O(n * IO) instead of a single bulk fetch.
 *
 * Uses heuristics to identify repository calls (method name patterns + target name patterns).
 * Users can extend what counts as a repository call via `.algorilla.yml`.
 */
public class NPlusOneRepositoryCallRule : Rule {
    override val id: String = "n-plus-one-query"
    override val name: String = "N+1 Query"
    override val aliases: List<String> = listOf("n-plus-one-repository-call")
    override val severity: Severity = Severity.WARNING
    override val languages: Set<Language> = Language.entries.toSet()
    override val category: RuleCategory = RuleCategory.QUERY_PATTERN

    override fun evaluate(context: AnalysisContext): List<Finding> {
        val findings = mutableListOf<Finding>()
        for ((_, fileRoot) in context.irTrees) {
            scanNode(fileRoot, emptyList(), context, findings)
        }
        return findings
    }

    private fun scanNode(
        node: IRNode,
        loopStack: List<LoopNode>,
        context: AnalysisContext,
        findings: MutableList<Finding>,
    ) {
        if (node is LoopNode) {
            for (child in node.children) {
                scanNode(child, loopStack + node, context, findings)
            }
            return
        }

        if (loopStack.isNotEmpty() && node is FunctionCall) {
            if (isSingleRecordFetch(node)) {
                findings.add(buildFinding(node, loopStack))
            } else {
                // Cross-method: check if a helper method internally calls a repository method
                val maxDepth = context.config.maxCallDepth.coerceAtMost(2)
                val hiddenFetch =
                    CrossMethodResolver.resolveAndFind<FunctionCall>(
                        node,
                        context.symbolTable,
                        maxDepth = maxDepth,
                    ) { isSingleRecordFetch(it) }
                if (hiddenFetch != null) {
                    findings.add(buildCrossMethodFinding(node, hiddenFetch, loopStack))
                }
            }
        }

        for (child in node.children) {
            scanNode(child, loopStack, context, findings)
        }
    }

    private fun buildCrossMethodFinding(
        call: FunctionCall,
        hiddenFetch: FunctionCall,
        loopStack: List<LoopNode>,
    ): Finding {
        val outerLoop = loopStack.first()
        val loopVar = outerLoop.iteratedVariable ?: "items"
        val target = hiddenFetch.qualifiedTarget ?: "repository"
        val evidence =
            listOf(
                Evidence(outerLoop.location, outerLoop.kind.label(), ExecutionContext.INSIDE_LOOP, complexity = "O(|$loopVar|)"),
                Evidence(call.location, "${call.name}() called per iteration", ExecutionContext.INSIDE_LOOP, depth = 1),
                Evidence(
                    hiddenFetch.location,
                    "$target.${hiddenFetch.name}() inside ${call.name}()",
                    ExecutionContext.INSIDE_LOOP,
                    depth = 2,
                    complexity = "IO \u2190 bottleneck",
                ),
            )
        return Finding(
            ruleId = id,
            ruleName = name,
            severity = severity,
            location = call.location,
            message = "Single-record fetch $target.${hiddenFetch.name}() inside ${call.name}() called from ${outerLoop.kind.label()} (N+1)",
            suggestion = "Bulk fetch all needed records before the loop, or build an in-memory Map",
            currentComplexity = "O(|$loopVar| * IO)",
            suggestedComplexity = "O(1 * IO + |$loopVar|)",
            evidence = evidence,
        )
    }

    private fun buildFinding(
        call: FunctionCall,
        loopStack: List<LoopNode>,
    ): Finding {
        val outerLoop = loopStack.first()
        val loopVar = outerLoop.iteratedVariable ?: "items"
        val target = call.qualifiedTarget ?: "repository"
        val evidence =
            listOf(
                Evidence(outerLoop.location, outerLoop.kind.label(), ExecutionContext.INSIDE_LOOP, complexity = "O(|$loopVar|)"),
                Evidence(
                    call.location,
                    "$target.${call.name}() called per iteration",
                    ExecutionContext.INSIDE_LOOP,
                    depth = 1,
                    complexity = "IO \u2190 bottleneck",
                ),
            )
        return Finding(
            ruleId = id,
            ruleName = name,
            severity = severity,
            location = call.location,
            message = "Single-record fetch $target.${call.name}() inside ${outerLoop.kind.label()} (N+1)",
            suggestion = "Bulk fetch all needed records before the loop, or build an in-memory Map",
            currentComplexity = "O(|$loopVar| * IO)",
            suggestedComplexity = "O(1 * IO + |$loopVar|)",
            evidence = evidence,
        )
    }
}

private val SINGLE_FETCH_METHOD_PREFIXES =
    listOf(
        "findById",
        "getById",
        "findOne",
        "getOne",
        "loadById",
        "findByKey",
        "getByKey",
        "loadByKey",
        "countBy",
    )

/** Patterns like getAnimalByAnimalId, findUserByEmail — strong signal for single-record fetch */
private val SINGLE_FETCH_METHOD_REGEX =
    Regex(
        """^(?:get|find|load|fetch|lookup)\w+By\w+(?:Id|Key|Name|Code|Identifier)$""",
        RegexOption.IGNORE_CASE,
    )

private val REPO_TARGET_PATTERNS =
    listOf(
        "repository",
        "repo",
        "dao",
        "store",
        "service",
        "client",
        "proxy",
    )

private fun isSingleRecordFetch(call: FunctionCall): Boolean {
    val matchesMethod = SINGLE_FETCH_METHOD_PREFIXES.any { call.name.startsWith(it, ignoreCase = true) }
    if (matchesMethod) {
        val target = call.qualifiedTarget?.lowercase()
        if (target == null || REPO_TARGET_PATTERNS.any { target.contains(it) }) return true
    }
    // Also match strong naming patterns like getAnimalByAnimalId, findUserByEmail
    if (SINGLE_FETCH_METHOD_REGEX.matches(call.name)) return true
    return false
}
