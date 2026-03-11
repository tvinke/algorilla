package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.model.ExecutionContext
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LoopNode
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Evidence
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.Rule
import com.github.tvinke.algorilla.rules.RuleCategory
import com.github.tvinke.algorilla.semantics.CollectionSemanticsRegistry
import com.github.tvinke.algorilla.util.hasO1Type

/**
 * Detects element-by-element removal from List/Array inside loops. Each remove() on an
 * ArrayList shifts all subsequent elements, making it O(n) per removal and O(n²) overall.
 *
 * Covers: remove(), removeFirst(), removeLast() on collections, and splice() in JS/TS.
 */
public class QuadraticRemovalRule : Rule {
    override val id: String = "quadratic-removal"
    override val name: String = "Quadratic Removal"
    override val severity: Severity = Severity.WARNING
    override val languages: Set<Language> = Language.entries.toSet()
    override val category: RuleCategory = RuleCategory.LOOP_AMPLIFIER

    override fun evaluate(context: AnalysisContext): List<Finding> {
        val findings = mutableListOf<Finding>()
        for ((_, fileRoot) in context.irTrees) {
            scanNode(fileRoot, null, emptyList(), findings)
        }
        return findings
    }

    private fun scanNode(
        node: IRNode,
        enclosingFn: FunctionDecl?,
        loopStack: List<LoopNode>,
        findings: MutableList<Finding>,
    ) {
        val fn = if (node is FunctionDecl) node else enclosingFn

        if (node is LoopNode) {
            for (child in node.children) {
                scanNode(child, fn, loopStack + node, findings)
            }
            return
        }

        if (loopStack.isNotEmpty() && node is FunctionCall) {
            if (isRemovalCall(node) && !isO1RemovalTarget(node, fn)) {
                findings.add(buildFinding(node, loopStack))
            }
        }

        for (child in node.children) {
            scanNode(child, fn, loopStack, findings)
        }
    }

    private fun isO1RemovalTarget(
        call: FunctionCall,
        fn: FunctionDecl?,
    ): Boolean = fn != null && fn.hasO1Type(call.qualifiedTarget)

    private fun buildFinding(
        call: FunctionCall,
        loopStack: List<LoopNode>,
    ): Finding {
        val outerLoop = loopStack.first()
        val loopVar = outerLoop.iteratedVariable ?: "items"
        val target = call.qualifiedTarget ?: "list"
        val evidence =
            listOf(
                Evidence(outerLoop.location, outerLoop.kind.label(), ExecutionContext.INSIDE_LOOP, complexity = "O(|$loopVar|)"),
                Evidence(
                    call.location,
                    "$target.${call.name}() inside loop",
                    ExecutionContext.INSIDE_LOOP,
                    depth = 1,
                    complexity = "shift ← bottleneck",
                ),
            )
        return Finding(
            ruleId = id,
            ruleName = name,
            severity = severity,
            location = call.location,
            message = "${call.name}() on '$target' inside ${outerLoop.kind.label()} shifts elements on each removal",
            suggestion = "Use removeAll() with a Set, Iterator.remove(), or filter into a new collection",
            currentComplexity = "O(|$loopVar|²)",
            suggestedComplexity = "O(|$loopVar|)",
            evidence = evidence,
        )
    }
}

// removeFirst/removeLast are excluded — they strongly indicate Queue/Deque usage (O(1))
private val REMOVAL_METHODS: Set<String> by lazy {
    CollectionSemanticsRegistry.loadDefaults().allRemovalMethods()
}

/** Skip calls on Map targets (map.remove(key) is O(1)) and iterator.remove(). */
private val NON_LIST_TARGET_SUFFIXES =
    setOf(
        "map",
        "hashmap",
        "treemap",
        "concurrenthashmap",
        "iterator",
        "iter",
        "entry",
        "set",
        "hashset",
        "treeset",
        "queue",
        "deque",
        "stack",
        "linkedlist",
    )

private val NON_LIST_EXACT_TARGETS = setOf("it", "map", "set", "entry", "iter", "queue", "stack")

private fun isRemovalCall(call: FunctionCall): Boolean {
    if (call.name !in REMOVAL_METHODS) return false
    val target = call.qualifiedTarget ?: return false
    val lower = target.lowercase()
    if (lower in NON_LIST_EXACT_TARGETS) return false
    return NON_LIST_TARGET_SUFFIXES.none { suffix -> lower.endsWith(suffix) }
}
