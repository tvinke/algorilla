package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.model.Confidence
import com.github.tvinke.algorilla.model.ExecutionContext
import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LoopNode
import com.github.tvinke.algorilla.model.ObjectCreation
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.model.VariableDecl
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.ComplexityModel
import com.github.tvinke.algorilla.rules.Evidence
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.Rule
import com.github.tvinke.algorilla.rules.RuleCategory
import com.github.tvinke.algorilla.util.findDescendants
import com.github.tvinke.algorilla.util.referencesName

/**
 * Detects function calls inside loop bodies that don't depend on the loop variable
 * and could be hoisted before the loop. Each iteration repeats the same computation.
 *
 * Uses [referencesName] to check if a call's arguments or target reference the
 * loop variable or any variable declared inside the loop body. Excludes IO methods
 * (side effects), mutation methods, and cheap/trivial methods (not worth hoisting).
 */
public class LoopInvariantHoistingRule : Rule {
    override val id: String = "loop-invariant-hoisting"
    override val name: String = "Loop-Invariant Hoisting"
    override val severity: Severity = Severity.INFO
    override val defaultConfidence: Confidence = Confidence.LOW
    override val languages: Set<Language> = Language.entries.toSet()
    override val category: RuleCategory = RuleCategory.REDUNDANCY

    override fun evaluate(context: AnalysisContext): List<Finding> {
        val findings = mutableListOf<Finding>()
        for ((_, fileRoot) in context.irTrees) {
            val language = (fileRoot as? FileRoot)?.language ?: Language.JAVA
            val skipMethods = collectSkipMethods(language, context)
            for (fn in fileRoot.findDescendants<FunctionDecl>()) {
                scanNode(fn, fn.children, emptyList(), skipMethods, findings)
            }
        }
        return findings
    }

    private fun scanNode(
        enclosingFn: FunctionDecl,
        nodes: List<IRNode>,
        loopStack: List<LoopNode>,
        skipMethods: Set<String>,
        findings: MutableList<Finding>,
    ) {
        for (node in nodes) {
            if (node is LoopNode) {
                scanNode(enclosingFn, node.children, loopStack + node, skipMethods, findings)
                continue
            }

            if (loopStack.isNotEmpty() && node is FunctionCall) {
                checkLoopInvariant(node, loopStack, skipMethods, findings)
            }

            scanNode(enclosingFn, node.children, loopStack, skipMethods, findings)
        }
    }

    @Suppress("ReturnCount")
    private fun checkLoopInvariant(
        call: FunctionCall,
        loopStack: List<LoopNode>,
        skipMethods: Set<String>,
        findings: MutableList<Finding>,
    ) {
        // Skip methods that are side effects, trivial, or explicitly cheap
        if (call.name in skipMethods) return

        // Skip calls with no target (bare function calls are often control flow)
        if (call.qualifiedTarget == null && call.arguments.isEmpty()) return

        val innerLoop = loopStack.last()
        val loopVar = innerLoop.iteratedVariable ?: return

        // Collect all variable names defined inside the loop body
        val loopLocalVars = collectLoopLocalVars(innerLoop)

        // Check if the call references the loop variable or any loop-local variable
        val dependentNames = setOf(loopVar) + loopLocalVars
        if (callDependsOn(call, dependentNames)) return

        // Skip constructors — already caught by heavyweight-object-per-invocation
        if (call.children.any { it is ObjectCreation }) return

        findings.add(buildFinding(call, loopStack))
    }

    @Suppress("ReturnCount")
    private fun callDependsOn(
        call: FunctionCall,
        names: Set<String>,
    ): Boolean {
        // Check qualifiedTarget
        if (call.qualifiedTarget != null && call.qualifiedTarget in names) return true

        // Check arguments
        for (arg in call.arguments) {
            if (names.any { arg.referencesName(it) }) return true
            if (anyDescendantReferences(arg, names)) return true
        }

        // Check children (lambda bodies etc.)
        for (child in call.children) {
            if (names.any { child.referencesName(it) }) return true
            if (anyDescendantReferences(child, names)) return true
        }

        return false
    }

    private fun anyDescendantReferences(
        node: IRNode,
        names: Set<String>,
    ): Boolean {
        for (child in node.children) {
            if (names.any { child.referencesName(it) }) return true
            if (anyDescendantReferences(child, names)) return true
        }
        return false
    }

    private fun collectLoopLocalVars(loop: LoopNode): Set<String> = loop.findDescendants<VariableDecl>().map { it.name }.toSet()

    private fun collectSkipMethods(
        language: Language,
        context: AnalysisContext,
    ): Set<String> {
        val skip = mutableSetOf<String>()
        skip.addAll(context.registry.ioMethods(language))
        skip.addAll(context.registry.allMutationMethods())
        skip.addAll(context.registry.allCheapMethods())
        skip.addAll(context.registry.allTrivialMethods())
        skip.addAll(context.registry.allBuilderMethods())
        return skip
    }

    @Suppress("LongMethod")
    private fun buildFinding(
        call: FunctionCall,
        loopStack: List<LoopNode>,
    ): Finding {
        val outerLoop = loopStack.first()
        val loopVar = outerLoop.iteratedVariable ?: "items"
        val target = call.qualifiedTarget?.let { "$it." } ?: ""
        return Finding(
            ruleId = id,
            ruleName = name,
            severity = severity,
            location = call.location,
            message = "${target}${call.name}() inside ${outerLoop.kind.label()} does not depend on '$loopVar'",
            suggestion = "Hoist ${target}${call.name}() before the loop — the result is the same on every iteration",
            currentComplexity = ComplexityModel.redundantCalls(0).current.replace("0x", "|$loopVar|x"),
            suggestedComplexity = "O(1)",
            evidence =
                listOf(
                    Evidence(
                        outerLoop.location,
                        outerLoop.kind.label(),
                        ExecutionContext.INSIDE_LOOP,
                        complexity = "O(|$loopVar|)",
                    ),
                    Evidence(
                        call.location,
                        "${target}${call.name}() — loop-invariant",
                        ExecutionContext.INSIDE_LOOP,
                        depth = 1,
                        complexity = "repeated \u2190 hoist",
                    ),
                ),
        )
    }
}
