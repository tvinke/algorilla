package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.model.ExecutionContext
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.ObjectCreation
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Evidence
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.Rule
import com.github.tvinke.algorilla.rules.RuleCategory
import com.github.tvinke.algorilla.util.findDescendants

/**
 * Detects creation of heavyweight objects (e.g. ObjectMapper, Gson) inside method bodies.
 * These objects are expensive to instantiate and should be reused as static fields
 * or injected via dependency injection.
 *
 * Constructors are excluded — it's normal to initialize heavyweight objects in a constructor.
 */
public class HeavyweightObjectPerInvocationRule : Rule {
    override val id: String = "expensive-construction"
    override val name: String = "Expensive Construction"
    override val aliases: List<String> = listOf("heavyweight-object-per-invocation")
    override val severity: Severity = Severity.INFO
    override val languages: Set<Language> = Language.entries.toSet()
    override val category: RuleCategory = RuleCategory.CONSTRUCTION_COST

    override fun evaluate(context: AnalysisContext): List<Finding> {
        val heavyTypes = context.registry.allHeavyweightTypes()
        val findings = mutableListOf<Finding>()
        for ((_, fileRoot) in context.irTrees) {
            scanNode(fileRoot, heavyTypes, findings)
        }
        return findings
    }

    private fun scanNode(
        node: IRNode,
        heavyTypes: Set<String>,
        findings: MutableList<Finding>,
    ) {
        if (node is FunctionDecl && !node.isConstructor) {
            checkFunction(node, heavyTypes, findings)
        }
        for (child in node.children) {
            scanNode(child, heavyTypes, findings)
        }
    }

    private fun checkFunction(
        fn: FunctionDecl,
        heavyTypes: Set<String>,
        findings: MutableList<Finding>,
    ) {
        val creations = fn.findDescendants<ObjectCreation>()
        val matches = creations.filter { it.typeName in heavyTypes }
        for (creation in matches) {
            findings.add(buildFinding(fn, creation))
        }
    }

    private fun buildFinding(
        fn: FunctionDecl,
        creation: ObjectCreation,
    ): Finding {
        val evidence =
            listOf(
                Evidence(
                    location = creation.location,
                    label = "new ${creation.typeName}() in ${fn.name}()",
                    executionContext = ExecutionContext.SINGLE,
                ),
            )
        return Finding(
            ruleId = id,
            ruleName = name,
            severity = severity,
            location = creation.location,
            message = "${creation.typeName} created inside ${fn.name}() on every invocation",
            suggestion = "Reuse as a static final field or inject via dependency injection",
            currentComplexity = "O(n * init)",
            suggestedComplexity = "O(1)",
            evidence = evidence,
        )
    }
}
