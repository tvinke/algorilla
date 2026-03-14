package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.model.Confidence
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

/**
 * Detects String.concat() calls inside loops. Each concat() creates a new String object,
 * copying the entire accumulated content — turning an O(n) loop into O(n²).
 *
 * Note: This rule detects explicit .concat() calls. The more common `+=` operator on strings
 * has the same O(n²) behavior but requires parser-level support to detect.
 */
public class StringConcatInLoopRule : Rule {
    override val id: String = "string-concat-in-loop"
    override val name: String = "String Concat In Loop"
    override val severity: Severity = Severity.WARNING
    override val languages: Set<Language> = setOf(Language.JAVA, Language.KOTLIN, Language.GROOVY)
    override val category: RuleCategory = RuleCategory.LOOP_AMPLIFIER
    override val defaultConfidence: Confidence = Confidence.HIGH
    override val requiresTypeContext: Boolean = true

    override fun evaluate(context: AnalysisContext): List<Finding> {
        val findings = mutableListOf<Finding>()
        for ((_, fileRoot) in context.irTrees) {
            // Skip JS/TS — Array.concat() is not string concatenation
            if (fileRoot.language !in languages) continue
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
            for (child in node.children) {
                scanNode(child, loopStack + node, findings)
            }
            return
        }

        if (loopStack.isNotEmpty() && node is FunctionCall && isStringConcatCall(node)) {
            findings.add(buildFinding(node, loopStack))
        }

        for (child in node.children) {
            scanNode(child, loopStack, findings)
        }
    }

    private fun buildFinding(
        call: FunctionCall,
        loopStack: List<LoopNode>,
    ): Finding {
        val outerLoop = loopStack.first()
        val loopVar = outerLoop.iteratedVariable ?: "items"
        val target = call.qualifiedTarget ?: "string"
        val evidence =
            listOf(
                Evidence(outerLoop.location, outerLoop.kind.label(), ExecutionContext.INSIDE_LOOP, complexity = "O(|$loopVar|)"),
                Evidence(
                    call.location,
                    "$target.${call.name}() inside loop",
                    ExecutionContext.INSIDE_LOOP,
                    depth = 1,
                    complexity = "copy ← bottleneck",
                ),
            )
        return Finding(
            ruleId = id,
            ruleName = name,
            severity = severity,
            location = call.location,
            message = "String ${call.name}() inside ${outerLoop.kind.label()} copies the entire string on each iteration",
            suggestion = "Use a StringBuilder to accumulate the result, then call toString() after the loop",
            currentComplexity = "O(|$loopVar|²)",
            suggestedComplexity = "O(|$loopVar|)",
            evidence = evidence,
        )
    }
}

private val STRING_CONCAT_METHODS = setOf("concat")

// Receiver names that indicate non-String .concat() — typed objects like factories, builders, collections
private val NON_STRING_RECEIVER_PATTERNS =
    setOf("factory", "builder", "attributes", "immutable", "collector", "stream")

private fun isStringConcatCall(call: FunctionCall): Boolean {
    if (call.name !in STRING_CONCAT_METHODS) return false
    val target = call.qualifiedTarget ?: return false
    // Skip when the receiver name suggests a typed object (factory, builder, etc.)
    val lower = target.lowercase()
    if (NON_STRING_RECEIVER_PATTERNS.any { lower.contains(it) }) return false
    return true
}
