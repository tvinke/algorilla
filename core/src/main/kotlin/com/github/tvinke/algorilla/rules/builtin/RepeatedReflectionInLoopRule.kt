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
import com.github.tvinke.algorilla.rules.Suggestion

/**
 * Detects Java/Kotlin reflection calls inside loops. Reflection methods like
 * getDeclaredMethods(), getDeclaredFields(), getAnnotation() are 10-100x slower
 * than direct access and allocate new arrays on each call. Results should be
 * cached outside the loop.
 */
public class RepeatedReflectionInLoopRule : Rule {
    override val id: String = "repeated-reflection-in-loop"
    override val name: String = "Repeated Reflection In Loop"
    override val severity: Severity = Severity.INFO
    override val languages: Set<Language> = setOf(Language.JAVA, Language.KOTLIN, Language.GROOVY)
    override val category: RuleCategory = RuleCategory.LOOP_AMPLIFIER

    override fun evaluate(context: AnalysisContext): List<Finding> {
        val findings = mutableListOf<Finding>()
        for ((_, fileRoot) in context.irTrees) {
            val reflectionMethods = context.registry.reflectionMethods(fileRoot.language)
            val reflectionExclusions = context.registry.reflectionExclusions(fileRoot.language)
            scanNode(fileRoot, emptyList(), reflectionMethods, reflectionExclusions, findings)
        }
        return findings
    }

    private fun scanNode(
        node: IRNode,
        loopStack: List<LoopNode>,
        reflectionMethods: Set<String>,
        reflectionExclusions: Set<String>,
        findings: MutableList<Finding>,
    ) {
        if (node is LoopNode) {
            for (child in node.children) {
                scanNode(child, loopStack + node, reflectionMethods, reflectionExclusions, findings)
            }
            return
        }

        if (loopStack.isNotEmpty() && node is FunctionCall && isReflectionCall(node, reflectionMethods, reflectionExclusions)) {
            findings.add(buildFinding(node, loopStack))
        }

        for (child in node.children) {
            scanNode(child, loopStack, reflectionMethods, reflectionExclusions, findings)
        }
    }

    @Suppress("LongMethod") // Assembles reflection-specific finding with cost evidence
    private fun buildFinding(
        call: FunctionCall,
        loopStack: List<LoopNode>,
    ): Finding {
        val outerLoop = loopStack.first()
        val loopVar = outerLoop.iteratedVariable ?: "items"
        val target = call.qualifiedTarget ?: "class"
        val evidence =
            listOf(
                Evidence(outerLoop.location, outerLoop.kind.label(), ExecutionContext.INSIDE_LOOP, complexity = "O($loopVar)"),
                Evidence(
                    call.location,
                    "$target.${call.name}() inside loop",
                    ExecutionContext.INSIDE_LOOP,
                    depth = 1,
                    complexity = "reflection ← bottleneck",
                ),
            )
        return Finding(
            ruleId = id,
            ruleName = name,
            severity = severity,
            location = call.location,
            message = "Reflection call ${call.name}() inside ${outerLoop.kind.label()} — reflection is 10-100× slower than direct access",
            suggestions =
                listOf(
                    Suggestion.Freeform("Cache the reflection result in a local variable or Map<Class, ...> outside the loop"),
                ),
            currentComplexity = "O($loopVar × reflection)",
            suggestedComplexity = "O($loopVar)",
            evidence = evidence,
        )
    }
}

private fun isReflectionCall(
    call: FunctionCall,
    reflectionMethods: Set<String>,
    reflectionExclusions: Set<String>,
): Boolean = call.name in reflectionMethods && call.name !in reflectionExclusions
