package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.model.Confidence
import com.github.tvinke.algorilla.model.ExecutionContext
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LoopNode
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.ComplexityModel
import com.github.tvinke.algorilla.rules.Evidence
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.Rule
import com.github.tvinke.algorilla.rules.RuleCategory
import com.github.tvinke.algorilla.rules.Suggestion
import com.github.tvinke.algorilla.util.MemoizationDetector
import com.github.tvinke.algorilla.util.findDescendants

/**
 * Detects recursive functions where the same subproblem may be solved multiple times
 * because there is no caching or memoization. Classic example: naive Fibonacci.
 *
 * Applies false-positive filters for tree traversals (where the recursive call uses
 * a child-access argument like node.left) and functions that already have memoization
 * or visited-set tracking in place.
 */
public class UnmemoizedRecursionRule : Rule {
    override val id: String = "unmemoized-recursion"
    override val name: String = "Unmemoized Recursion"
    override val severity: Severity = Severity.WARNING
    override val defaultConfidence: Confidence = Confidence.LOW
    override val languages: Set<Language> = Language.entries.toSet()
    override val category: RuleCategory = RuleCategory.REDUNDANCY

    override fun evaluate(context: AnalysisContext): List<Finding> {
        val findings = mutableListOf<Finding>()
        for ((_, fileRoot) in context.irTrees) {
            val language = fileRoot.language
            val functions = fileRoot.findDescendants<FunctionDecl>()
            for (fn in functions) {
                checkFunction(fn, language, context, findings)
            }
        }
        return findings
    }

    // Three distinct recursion patterns (overlapping subproblem, loop-recursive, single) each with unique findings
    @Suppress("LongMethod", "ReturnCount")
    private fun checkFunction(
        fn: FunctionDecl,
        language: Language,
        context: AnalysisContext,
        findings: MutableList<Finding>,
    ) {
        // Skip standard object methods — recursive by nature in entity hierarchies
        if (fn.name in context.registry.objectMethods(language)) return

        val recursiveCalls = fn.findDescendants<FunctionCall>().filter { it.name == fn.name }
        if (recursiveCalls.isEmpty()) return

        // Already memoized — skip
        if (MemoizationDetector.hasMemoizationPattern(fn, language, context.registry)) return

        // Has visited-set tracking — skip
        if (MemoizationDetector.hasVisitedTracking(fn, language, context.registry)) return

        // Classify the recursion pattern
        val callsInLoops = recursiveCalls.filter { isInsideLoop(it, fn) }
        val topLevelCalls = recursiveCalls - callsInLoops.toSet()

        // Check if multiple recursive calls exist outside loops (classic overlapping subproblem)
        if (topLevelCalls.size >= 2) {
            // Filter: if ALL calls use tree-traversal accessors, this is a tree walk
            if (topLevelCalls.all { usesTreeTraversalAccessor(it, language, context) }) return

            val paramName = fn.parameters.firstOrNull()?.name ?: "n"
            val cx = ComplexityModel.exponentialRecursion(paramName)
            findings.add(
                Finding(
                    ruleId = id,
                    ruleName = name,
                    severity = Severity.WARNING,
                    confidence = Confidence.HIGH,
                    location = fn.location,
                    message =
                        "${fn.name}() has ${topLevelCalls.size} recursive calls without memoization " +
                            "\u2014 overlapping subproblems cause exponential re-computation",
                    suggestions =
                        listOf(
                            Suggestion.Freeform(
                                "Add a cache (Map/HashMap) keyed on the function arguments to avoid " +
                                    "recomputing the same subproblem",
                            ),
                        ),
                    currentComplexity = cx.current,
                    suggestedComplexity = cx.suggested,
                    evidence =
                        buildList {
                            add(Evidence(fn.location, "${fn.name}() — recursive function", ExecutionContext.SINGLE))
                            topLevelCalls.forEachIndexed { idx, call ->
                                add(Evidence(call.location, "recursive call #${idx + 1}", ExecutionContext.SINGLE, depth = 1))
                            }
                        },
                ),
            )
            return
        }

        // Recursive call inside a loop body
        if (callsInLoops.isNotEmpty()) {
            val call = callsInLoops.first()
            // If the argument is a child-access (tree traversal), skip
            if (usesTreeTraversalAccessor(call, language, context)) return

            val paramName = fn.parameters.firstOrNull()?.name ?: "n"
            findings.add(
                Finding(
                    ruleId = id,
                    ruleName = name,
                    severity = Severity.WARNING,
                    confidence = Confidence.MEDIUM,
                    location = call.location,
                    message = "${fn.name}() called recursively inside a loop without memoization",
                    suggestions =
                        listOf(
                            Suggestion.Freeform(
                                "Consider adding a visited set or memoization cache to avoid " +
                                    "re-processing the same input",
                            ),
                        ),
                    currentComplexity = "O($paramName \u00d7 recursion)",
                    suggestedComplexity = "O($paramName) with memo",
                    evidence =
                        listOf(
                            Evidence(fn.location, "${fn.name}() — recursive function", ExecutionContext.SINGLE),
                            Evidence(call.location, "recursive call inside loop body", ExecutionContext.INSIDE_LOOP, depth = 1),
                        ),
                ),
            )
            return
        }

        // Single recursive call, not in a loop
        if (topLevelCalls.size == 1) {
            val call = topLevelCalls.first()
            // Skip tree traversals entirely
            if (usesTreeTraversalAccessor(call, language, context)) return

            // Single non-child-access recursive call: low confidence, just informational
            val paramName = fn.parameters.firstOrNull()?.name ?: "n"
            findings.add(
                Finding(
                    ruleId = id,
                    ruleName = name,
                    severity = Severity.INFO,
                    confidence = Confidence.LOW,
                    location = call.location,
                    message = "${fn.name}() is recursive without memoization",
                    suggestions =
                        listOf(
                            Suggestion.Freeform(
                                "If this function can be called with the same arguments multiple times, " +
                                    "consider adding memoization",
                            ),
                        ),
                    currentComplexity = null,
                    suggestedComplexity = null,
                    evidence =
                        listOf(
                            Evidence(fn.location, "${fn.name}() — recursive function", ExecutionContext.SINGLE),
                            Evidence(call.location, "single recursive call without memo", ExecutionContext.SINGLE, depth = 1),
                        ),
                ),
            )
        }
    }

    /**
     * Checks whether a recursive call passes a tree-traversal accessor as argument
     * (e.g. node.left, node.right, node.children).
     */
    private fun usesTreeTraversalAccessor(
        call: FunctionCall,
        language: Language,
        context: AnalysisContext,
    ): Boolean {
        val registry = context.registry
        // Check the call's arguments for qualified targets that are tree accessors
        for (arg in call.arguments) {
            if (arg is FunctionCall && registry.isTreeTraversalAccessor(language, arg.name)) {
                return true
            }
        }
        // Also check direct children (for property-access style like node.left)
        for (child in call.children) {
            if (child is FunctionCall && registry.isTreeTraversalAccessor(language, child.name)) {
                return true
            }
        }
        return false
    }

    /**
     * Returns true if the given call node is nested inside a [LoopNode] within the function.
     */
    private fun isInsideLoop(
        call: FunctionCall,
        fn: FunctionDecl,
    ): Boolean {
        // Walk the function's descendants and track loop nesting
        return isDescendantOfLoop(fn.children, call)
    }

    private fun isDescendantOfLoop(
        nodes: List<com.github.tvinke.algorilla.model.IRNode>,
        target: FunctionCall,
    ): Boolean {
        for (node in nodes) {
            if (node === target) return false // found target before any loop
            if (node is LoopNode) {
                if (containsNode(node.children, target)) return true
            }
            if (isDescendantOfLoop(node.children, target)) return true
        }
        return false
    }

    private fun containsNode(
        nodes: List<com.github.tvinke.algorilla.model.IRNode>,
        target: FunctionCall,
    ): Boolean {
        for (node in nodes) {
            if (node === target) return true
            if (containsNode(node.children, target)) return true
        }
        return false
    }
}
