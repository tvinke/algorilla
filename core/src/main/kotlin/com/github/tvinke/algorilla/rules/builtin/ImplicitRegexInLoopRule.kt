package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.model.ExecutionContext
import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.GenericNode
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

/**
 * Detects String methods that internally compile a regex on every call when used inside loops.
 * Methods like String.matches(), split(), replaceAll(), and replaceFirst() call Pattern.compile()
 * internally, making each invocation O(pattern-length) for compilation alone.
 */
public class ImplicitRegexInLoopRule : Rule {
    override val id: String = "implicit-regex-in-loop"
    override val name: String = "Implicit Regex In Loop"
    override val severity: Severity = Severity.WARNING
    override val languages: Set<Language> = Language.entries.toSet()
    override val category: RuleCategory = RuleCategory.LOOP_AMPLIFIER

    override fun evaluate(context: AnalysisContext): List<Finding> {
        val findings = mutableListOf<Finding>()
        for ((_, fileRoot) in context.irTrees) {
            val language = (fileRoot as? FileRoot)?.language
            scanNode(fileRoot, emptyList(), language, findings)
        }
        return findings
    }

    private fun scanNode(
        node: IRNode,
        loopStack: List<LoopNode>,
        language: Language?,
        findings: MutableList<Finding>,
    ) {
        if (node is LoopNode) {
            for (child in node.children) {
                scanNode(child, loopStack + node, language, findings)
            }
            return
        }

        if (loopStack.isNotEmpty() && node is FunctionCall && isImplicitRegexCall(node, language)) {
            findings.add(buildFinding(node, loopStack))
        }

        for (child in node.children) {
            scanNode(child, loopStack, language, findings)
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
                    complexity = "compile ← bottleneck",
                ),
            )
        return Finding(
            ruleId = id,
            ruleName = name,
            severity = severity,
            location = call.location,
            message = "${call.name}() compiles a regex on every call inside ${outerLoop.kind.label()}",
            suggestion = "Pre-compile with Pattern.compile() outside the loop and use Matcher directly",
            currentComplexity = "O(|$loopVar| × compile)",
            suggestedComplexity = "O(|$loopVar|)",
            evidence = evidence,
        )
    }
}

private val IMPLICIT_REGEX_METHODS: Set<String> by lazy {
    CollectionSemanticsRegistry.loadDefaults().allImplicitRegexMethods()
}

/** Languages where string methods only compile regex when the argument is a regex literal. */
private val JS_FAMILY = setOf(Language.JAVASCRIPT, Language.TYPESCRIPT, Language.VUE)

private fun isImplicitRegexCall(
    call: FunctionCall,
    language: Language?,
): Boolean {
    if (call.name !in IMPLICIT_REGEX_METHODS || call.qualifiedTarget == null) return false
    // In JS/TS, split/replace/match with a plain string argument do NOT compile regex.
    // Only regex literals (/pattern/) or RegExp objects trigger compilation.
    if (language in JS_FAMILY && hasStringLiteralFirstArg(call)) return false
    return true
}

/**
 * Returns true if the first argument is a string literal (quoted with ' or ").
 * In the IR, arguments from the JS parser are [GenericNode] with the source text as nodeType.
 */
private fun hasStringLiteralFirstArg(call: FunctionCall): Boolean {
    val firstArg = call.arguments.firstOrNull() ?: return false
    if (firstArg !is GenericNode) return false
    val text = firstArg.nodeType.trim()
    return (text.startsWith("'") && text.endsWith("'")) ||
        (text.startsWith("\"") && text.endsWith("\"")) ||
        (text.startsWith("`") && text.endsWith("`"))
}
