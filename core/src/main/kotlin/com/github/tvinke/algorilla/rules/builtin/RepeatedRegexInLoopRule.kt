package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.model.Confidence
import com.github.tvinke.algorilla.model.ExecutionContext
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.GenericNode
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LoopNode
import com.github.tvinke.algorilla.model.ObjectCreation
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Evidence
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.Rule
import com.github.tvinke.algorilla.rules.RuleCategory
import com.github.tvinke.algorilla.rules.Suggestion
import com.github.tvinke.algorilla.semantics.TypeEnvironment
import com.github.tvinke.algorilla.util.endsWithAtWordBoundary
import com.github.tvinke.algorilla.util.walkLoopSites

/**
 * Detects regex pattern compilation inside loops. Compiling a regex is expensive;
 * the pattern should be compiled once outside the loop and reused.
 */
public class RepeatedRegexInLoopRule : Rule {
    override val id: String = "repeated-regex-in-loop"
    override val name: String = "Repeated Regex In Loop"
    override val severity: Severity = Severity.WARNING
    override val languages: Set<Language> = Language.entries.toSet()
    override val category: RuleCategory = RuleCategory.LOOP_AMPLIFIER
    override val defaultConfidence: Confidence = Confidence.HIGH

    override fun evaluate(context: AnalysisContext): List<Finding> {
        val findings = mutableListOf<Finding>()
        for ((_, fileRoot) in context.irTrees) {
            val regexTypes = context.registry.regexTypes(fileRoot.language)
            fileRoot.walkLoopSites { node, fn, loopStack ->
                if (node is ObjectCreation && node.typeName in regexTypes) {
                    findings.add(buildFinding(node, loopStack, "new ${node.typeName}()"))
                }
                if (node is FunctionCall && hasConstantArgument(node)) {
                    val typeEnv = fn?.let { context.typeEnvironmentFor(it) }
                    if (isCompileCall(node, typeEnv, regexTypes)) {
                        findings.add(buildFinding(node, loopStack, "${node.qualifiedTarget ?: "Pattern"}.${node.name}()"))
                    }
                }
            }
        }
        return findings
    }

    private fun buildFinding(
        node: IRNode,
        loopStack: List<LoopNode>,
        desc: String,
    ): Finding {
        val outerLoop = loopStack.first()
        val loopVar = outerLoop.iteratedVariable ?: "items"
        val evidence =
            listOf(
                Evidence(outerLoop.location, outerLoop.kind.label(), ExecutionContext.INSIDE_LOOP, complexity = "O($loopVar)"),
                Evidence(
                    node.location,
                    "$desc inside loop",
                    ExecutionContext.INSIDE_LOOP,
                    depth = 1,
                    complexity = "compile \u2190 bottleneck",
                ),
            )
        return Finding(
            ruleId = id,
            ruleName = name,
            severity = severity,
            location = node.location,
            message = "Regex compilation ($desc) inside ${outerLoop.kind.label()}",
            suggestions = listOf(Suggestion.Freeform("Compile the pattern once outside the loop and reuse the compiled Pattern")),
            currentComplexity = "O($loopVar * compile)",
            suggestedComplexity = "O($loopVar)",
            evidence = evidence,
        )
    }
}

// "Pattern"/"Regex" must be the receiver's own (qualified) name, not merely a substring
// somewhere in it — a class like DateTimePatternValidator.compile() has nothing to do
// with java.util.regex.Pattern, even though its name contains "Pattern". When the receiver's
// declared type is known, trust that over the name instead: a "userPattern" field typed as
// a domain class with its own compile() method isn't a regex compile just because the name
// ends in "Pattern".
private fun isCompileCall(
    call: FunctionCall,
    typeEnv: TypeEnvironment?,
    regexTypes: Set<String>,
): Boolean {
    if (call.name != "compile") return false
    val target = call.qualifiedTarget ?: return false
    val declaredType = typeEnv?.declaredTypeName(target)
    if (declaredType != null) return declaredType in regexTypes
    return endsWithAtWordBoundary(target, "Pattern") || endsWithAtWordBoundary(target, "Regex")
}

/**
 * Returns true if the compile() call's first argument appears to be a constant (string literal).
 * If the argument is a variable or expression, it likely varies per loop iteration and
 * cannot be hoisted — skip the finding.
 */
private fun hasConstantArgument(call: FunctionCall): Boolean {
    val firstArg = call.arguments.firstOrNull() ?: return true
    if (firstArg is GenericNode) {
        val text = firstArg.nodeType.trim()
        // String literal: starts and ends with quote
        if (text.startsWith("\"") || text.startsWith("'")) return true
        // Static constant: ALL_CAPS or qualified like Foo.PATTERN
        if (text.all { it == '_' || it.isUpperCase() || it == '.' }) return true
    }
    // Variable/expression arguments likely vary per iteration
    return false
}
