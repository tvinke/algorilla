package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.model.Confidence
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

/**
 * Detects String.concat() calls inside loops. Each concat() creates a new String object,
 * copying the entire accumulated content — turning an O(n) loop into O(n²).
 *
 * Uses the SymbolTable to check the receiver's type. If the type is known and NOT a
 * String-like type, the call is skipped (e.g. ImmutableAttributes.concat() is not
 * string concatenation). If the type is unknown, the call is still flagged.
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
            if (fileRoot.language !in languages) continue
            scanNode(fileRoot, null, emptyList(), context, findings)
        }
        return findings
    }

    private fun scanNode(
        node: IRNode,
        enclosingFn: FunctionDecl?,
        loopStack: List<LoopNode>,
        context: AnalysisContext,
        findings: MutableList<Finding>,
    ) {
        val fn = if (node is FunctionDecl) node else enclosingFn

        if (node is LoopNode) {
            for (child in node.children) {
                scanNode(child, fn, loopStack + node, context, findings)
            }
            return
        }

        if (loopStack.isNotEmpty() && node is FunctionCall) {
            if (isConcatCall(node) && !isNonStringReceiver(node.qualifiedTarget!!, fn, context)) {
                findings.add(buildFinding(node, loopStack))
            }
        }

        for (child in node.children) {
            scanNode(child, fn, loopStack, context, findings)
        }
    }

    private fun isConcatCall(call: FunctionCall): Boolean = call.name == "concat" && call.qualifiedTarget != null

    /**
     * Returns true if the receiver is known to be a non-String type via the SymbolTable.
     * When the type is unknown, returns false (assume it might be a String → flag it).
     */
    private fun isNonStringReceiver(
        target: String,
        fn: FunctionDecl?,
        context: AnalysisContext,
    ): Boolean {
        // Check SymbolTable for the receiver's declared type
        val declaredType = context.symbolTable.resolveType(target)
        if (declaredType != null) {
            return !isStringType(declaredType)
        }
        // Check function parameters for type info
        val paramType = fn?.parameters?.find { it.name == target }?.typeName
        if (paramType != null) {
            return !isStringType(paramType)
        }
        // Type unknown — conservatively assume it could be a String
        return false
    }

    private fun isStringType(typeName: String): Boolean {
        val lower = typeName.lowercase()
        return lower == "string" || lower == "charsequence" || lower == "stringbuilder" || lower == "stringbuffer"
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
                    complexity = "copy \u2190 bottleneck",
                ),
            )
        return Finding(
            ruleId = id,
            ruleName = name,
            severity = severity,
            location = call.location,
            message = "String ${call.name}() inside ${outerLoop.kind.label()} copies the entire string on each iteration",
            suggestion = "Use a StringBuilder to accumulate the result, then call toString() after the loop",
            currentComplexity = "O(|$loopVar|\u00b2)",
            suggestedComplexity = "O(|$loopVar|)",
            evidence = evidence,
        )
    }
}
