package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.model.ExecutionContext
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LoopNode
import com.github.tvinke.algorilla.model.ObjectCreation
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Evidence
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.Rule

/**
 * Detects regex pattern compilation inside loops. Compiling a regex is expensive;
 * the pattern should be compiled once outside the loop and reused.
 */
public class RepeatedRegexInLoopRule : Rule {
    override val id: String = "repeated-regex-in-loop"
    override val name: String = "Repeated Regex In Loop"
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
            for (child in node.children) {
                scanNode(child, loopStack + node, findings)
            }
            return
        }

        if (loopStack.isNotEmpty()) {
            if (node is ObjectCreation && isRegexType(node.typeName)) {
                findings.add(buildFinding(node, loopStack, "new ${node.typeName}()"))
            }
            if (node is FunctionCall && isCompileCall(node)) {
                findings.add(buildFinding(node, loopStack, "${node.qualifiedTarget ?: "Pattern"}.${node.name}()"))
            }
        }

        for (child in node.children) {
            scanNode(child, loopStack, findings)
        }
    }

    private fun buildFinding(
        node: IRNode,
        loopStack: List<LoopNode>,
        desc: String,
    ): Finding {
        val outerLoop = loopStack.first()
        val evidence = listOf(
            Evidence(outerLoop.location, outerLoop.kind.label(), ExecutionContext.INSIDE_LOOP),
            Evidence(node.location, "$desc inside loop", ExecutionContext.INSIDE_LOOP),
        )
        return Finding(
            ruleId = id, ruleName = name, severity = severity,
            location = node.location,
            message = "Regex compilation ($desc) inside ${outerLoop.kind.label()}",
            suggestion = "Compile the pattern once outside the loop and reuse the compiled Pattern",
            currentComplexity = "O(n * compile)", suggestedComplexity = "O(n)",
            evidence = evidence,
        )
    }
}

private val REGEX_TYPES = setOf("Pattern", "RegExp", "Regex")
private fun isRegexType(typeName: String): Boolean = typeName in REGEX_TYPES

private fun isCompileCall(call: FunctionCall): Boolean =
    call.name == "compile" && (call.qualifiedTarget?.contains("Pattern") == true ||
        call.qualifiedTarget?.contains("Regex") == true)
