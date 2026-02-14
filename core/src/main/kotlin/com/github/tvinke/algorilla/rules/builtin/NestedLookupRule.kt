package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.model.ExecutionContext
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LookupCall
import com.github.tvinke.algorilla.model.LoopNode
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Evidence
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.Rule

/**
 * Detects linear lookup operations (contains, indexOf, find, filter, etc.) inside loop bodies.
 * When a collection is searched linearly on every iteration of a loop, the combined complexity
 * becomes O(n*m) or O(n^2) where O(n) would suffice with a pre-built Set or Map.
 */
public class NestedLookupRule : Rule {
    override val id: String = "nested-lookup"
    override val name: String = "Nested Lookup"
    override val severity: Severity = Severity.WARNING
    override val languages: Set<Language> = Language.entries.toSet()

    override fun evaluate(context: AnalysisContext): List<Finding> {
        val findings = mutableListOf<Finding>()
        for ((_, fileRoot) in context.irTrees) {
            scanNode(fileRoot, emptyList(), findings)
        }
        return findings
    }

    private fun scanNode(
        node: IRNode,
        loopStack: List<LoopNode>,
        findings: MutableList<Finding>,
    ) {
        if (node is LoopNode) {
            val newStack = loopStack + node
            for (child in node.children) {
                scanNode(child, newStack, findings)
            }
            return
        }

        if (node is LookupCall && loopStack.isNotEmpty() && !node.isO1) {
            findings.add(buildFinding(node, loopStack))
        }

        for (child in node.children) {
            scanNode(child, loopStack, findings)
        }
    }

    private fun buildFinding(
        lookup: LookupCall,
        loopStack: List<LoopNode>,
    ): Finding {
        val targetVar = lookup.targetVariable ?: "collection"
        val outerLoop = loopStack.first()
        val evidence = buildEvidence(loopStack, lookup, targetVar)

        return Finding(
            ruleId = id,
            ruleName = name,
            severity = severity,
            location = outerLoop.location,
            message = "Linear ${lookup.kind.name.lowercase()} on '$targetVar' inside ${outerLoop.kind.label()}",
            suggestion = "Build a HashSet/Map from '$targetVar' before the loop",
            currentComplexity = "O(n*m)",
            suggestedComplexity = "O(n+m)",
            evidence = evidence,
        )
    }

    private fun buildEvidence(
        loopStack: List<LoopNode>,
        lookup: LookupCall,
        targetVar: String,
    ): List<Evidence> {
        val evidence =
            loopStack.map { loop ->
                Evidence(
                    location = loop.location,
                    label = "${loop.kind.label()} over ${loop.iteratedVariable ?: "items"}",
                    executionContext = ExecutionContext.INSIDE_LOOP,
                )
            }
        return evidence +
            Evidence(
                location = lookup.location,
                label = "${lookup.kind.name.lowercase()} on '$targetVar'",
                executionContext = ExecutionContext.INSIDE_LOOP,
            )
    }
}

internal fun com.github.tvinke.algorilla.model.LoopKind.label(): String =
    when (this) {
        com.github.tvinke.algorilla.model.LoopKind.FOR -> "for loop"
        com.github.tvinke.algorilla.model.LoopKind.WHILE -> "while loop"
        com.github.tvinke.algorilla.model.LoopKind.FOR_EACH -> "for-each loop"
        com.github.tvinke.algorilla.model.LoopKind.STREAM_FOR_EACH -> "stream().forEach()"
        com.github.tvinke.algorilla.model.LoopKind.HIGHER_ORDER -> "forEach()"
    }
