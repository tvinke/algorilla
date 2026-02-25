package com.github.tvinke.algorilla.rules.custom

import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.model.SourceLocation
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.Rule

/**
 * DSL builder for defining custom analysis rules in `.kts` script files.
 *
 * Example usage in a `.kts` file:
 * ```kotlin
 * rule("custom-001") {
 *     name = "My Custom Rule"
 *     severity = Severity.WARNING
 *     onNode<LoopNode> { node, file ->
 *         if (node.children.size > 10) {
 *             report(node.location, "Loop body is too large")
 *         }
 *     }
 * }
 * ```
 */
public class RuleBuilder(
    @PublishedApi internal val ruleId: String,
) {
    public var name: String = ruleId
    public var severity: Severity = Severity.WARNING
    public var suggestion: String = ""
    public var languages: Set<Language> = Language.entries.toSet()

    @PublishedApi
    internal var nodeVisitor: ((IRNode, String, MutableList<Finding>) -> Unit)? = null

    /**
     * Registers a visitor that is called for every IR node matching type [T].
     */
    public inline fun <reified T : IRNode> onNode(crossinline block: FindingReporter.(T, String) -> Unit) {
        nodeVisitor = { node, file, findings ->
            if (node is T) {
                val reporter = FindingReporter(ruleId, name, severity, suggestion, findings)
                reporter.block(node, file)
            }
        }
    }

    internal fun build(): Rule =
        DslRule(
            id = ruleId,
            name = name,
            severity = severity,
            languages = languages,
            nodeVisitor = nodeVisitor,
        )
}

/**
 * Helper passed to custom rule visitors for reporting findings.
 */
public class FindingReporter(
    private val ruleId: String,
    private val ruleName: String,
    private val severity: Severity,
    private val defaultSuggestion: String,
    private val findings: MutableList<Finding>,
) {
    /**
     * Reports a finding at the given source location.
     */
    public fun report(
        location: SourceLocation,
        message: String,
        suggestion: String = defaultSuggestion,
    ) {
        findings.add(
            Finding(
                ruleId = ruleId,
                ruleName = ruleName,
                severity = severity,
                location = location,
                message = message,
                suggestion = suggestion,
            ),
        )
    }
}

/**
 * Creates a custom rule using the DSL builder.
 */
public fun rule(
    id: String,
    block: RuleBuilder.() -> Unit,
): Rule = RuleBuilder(id).apply(block).build()

private class DslRule(
    override val id: String,
    override val name: String,
    override val severity: Severity,
    override val languages: Set<Language>,
    private val nodeVisitor: ((IRNode, String, MutableList<Finding>) -> Unit)?,
) : Rule {
    override fun evaluate(context: AnalysisContext): List<Finding> {
        val findings = mutableListOf<Finding>()
        if (nodeVisitor == null) return findings
        for ((file, fileRoot) in context.irTrees) {
            visitAll(fileRoot, file, findings)
        }
        return findings
    }

    private fun visitAll(
        node: IRNode,
        file: String,
        findings: MutableList<Finding>,
    ) {
        nodeVisitor?.invoke(node, file, findings)
        for (child in node.children) {
            visitAll(child, file, findings)
        }
    }
}
