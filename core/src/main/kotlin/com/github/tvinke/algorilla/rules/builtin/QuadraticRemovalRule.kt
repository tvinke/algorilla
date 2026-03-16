package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.model.Confidence
import com.github.tvinke.algorilla.model.ExecutionContext
import com.github.tvinke.algorilla.model.FileRoot
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
import com.github.tvinke.algorilla.rules.Suggestion
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
    override val defaultConfidence: Confidence = Confidence.MEDIUM
    override val requiresTypeContext: Boolean = true

    override fun evaluate(context: AnalysisContext): List<Finding> {
        val findings = mutableListOf<Finding>()
        for ((_, fileRoot) in context.irTrees) {
            val language = (fileRoot as? FileRoot)?.language
            val methods = language?.let { context.registry.removalMethods(it) } ?: emptySet()
            scanNode(fileRoot, null, emptyList(), methods, language, context, findings)
        }
        return findings
    }

    private fun scanNode(
        node: IRNode,
        enclosingFn: FunctionDecl?,
        loopStack: List<LoopNode>,
        removalMethods: Set<String>,
        language: Language?,
        context: AnalysisContext,
        findings: MutableList<Finding>,
    ) {
        val fn = if (node is FunctionDecl) node else enclosingFn

        if (node is LoopNode) {
            for (child in node.children) {
                scanNode(child, fn, loopStack + node, removalMethods, language, context, findings)
            }
            return
        }

        if (loopStack.isNotEmpty() && node is FunctionCall && isRemovalCall(node, fn, removalMethods, language, context)) {
            findings.add(buildFinding(node, loopStack))
        }

        for (child in node.children) {
            scanNode(child, fn, loopStack, removalMethods, language, context, findings)
        }
    }

    private fun buildFinding(
        call: FunctionCall,
        loopStack: List<LoopNode>,
    ): Finding {
        val outerLoop = loopStack.first()
        val loopVar = outerLoop.iteratedVariable ?: "items"
        val target = call.qualifiedTarget ?: "list"
        val evidence =
            listOf(
                Evidence(outerLoop.location, outerLoop.kind.label(), ExecutionContext.INSIDE_LOOP, complexity = "O($loopVar)"),
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
            suggestions = listOf(Suggestion.Freeform("Use removeAll() with a Set, Iterator.remove(), or filter into a new collection")),
            currentComplexity = "O($loopVar²)",
            suggestedComplexity = "O($loopVar)",
            evidence = evidence,
        )
    }
}

@Suppress("ReturnCount") // Guard clauses with early returns — clearer than nested if/else
private fun isRemovalCall(
    call: FunctionCall,
    enclosingFn: FunctionDecl?,
    removalMethods: Set<String>,
    language: Language?,
    context: AnalysisContext,
): Boolean {
    if (call.name !in removalMethods) return false
    val lang = language ?: Language.JAVA
    val registry = context.registry
    if (call.name in registry.bulkRemovalMethods(lang)) return false
    val target = call.qualifiedTarget ?: return false
    val lower = target.lowercase()
    if (lower in registry.nonListTargetsExact(lang).map { it.lowercase() }.toSet()) return false
    if (lower in registry.staticUtilityClasses(lang).map { it.lowercase() }.toSet()) return false
    if (registry.nonListTargetsContains(lang).any { lower.contains(it) }) return false
    if (registry.nonListTargetsSuffixes(lang).any { suffix -> lower.endsWith(suffix) }) return false
    // Use TypeEnvironment for full type resolution (field types, factory inference, chain-end)
    val typeEnv = enclosingFn?.let { context.typeEnvironmentFor(it) }
    if (typeEnv != null) return !typeEnv.isO1(target)
    // Fallback to basic parameter/local-decl check when no TypeEnvironment available
    return enclosingFn == null || !enclosingFn.hasO1Type(target)
}
