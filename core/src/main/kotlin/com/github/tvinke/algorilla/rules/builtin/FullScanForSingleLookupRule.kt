package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.model.CollectionAccess
import com.github.tvinke.algorilla.model.Confidence
import com.github.tvinke.algorilla.model.ExecutionContext
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LookupCall
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.ComplexityModel
import com.github.tvinke.algorilla.rules.Evidence
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.Rule
import com.github.tvinke.algorilla.rules.RuleCategory
import com.github.tvinke.algorilla.rules.Suggestion
import com.github.tvinke.algorilla.semantics.TypeEnvironment
import com.github.tvinke.algorilla.util.containsAnyAtWordBoundary
import com.github.tvinke.algorilla.util.findDescendants
import com.github.tvinke.algorilla.util.startsWithAtWordBoundary

/**
 * Detects loading all records from a data source then filtering for one in memory.
 * A targeted query is typically O(1) compared to loading everything at O(n).
 */
public class FullScanForSingleLookupRule : Rule {
    override val id: String = "bulk-load-for-single-lookup"
    override val name: String = "Bulk Load For Single Lookup"
    override val aliases: List<String> = listOf("full-scan-for-single-lookup")
    override val severity: Severity = Severity.WARNING
    override val languages: Set<Language> = Language.entries.toSet()
    override val category: RuleCategory = RuleCategory.QUERY_PATTERN
    override val defaultConfidence: Confidence = Confidence.LOW

    override fun evaluate(context: AnalysisContext): List<Finding> {
        val findings = mutableListOf<Finding>()
        for ((_, fileRoot) in context.irTrees) {
            val bulkLoadPrefixes = context.registry.bulkLoadPrefixes(fileRoot.language)
            val domTargets = context.registry.domTargetNames(fileRoot.language)
            scanNode(fileRoot, bulkLoadPrefixes, domTargets, context, findings)
        }
        return findings
    }

    private fun scanNode(
        node: IRNode,
        bulkLoadPrefixes: List<String>,
        domTargets: Set<String>,
        context: AnalysisContext,
        findings: MutableList<Finding>,
    ) {
        if (node is FunctionDecl) {
            checkFunction(node, bulkLoadPrefixes, domTargets, context, findings)
        }
        for (child in node.children) {
            scanNode(child, bulkLoadPrefixes, domTargets, context, findings)
        }
    }

    @Suppress("LongParameterList") // Threading the AnalysisContext through for TypeEnvironment lookups
    private fun checkFunction(
        fn: FunctionDecl,
        bulkLoadPrefixes: List<String>,
        domTargets: Set<String>,
        context: AnalysisContext,
        findings: MutableList<Finding>,
    ) {
        val typeEnv = context.typeEnvironmentFor(fn)
        val bulkCalls = fn.findDescendants<FunctionCall>().filter { isBulkLoadCall(it, bulkLoadPrefixes, domTargets, typeEnv) }
        if (bulkCalls.isEmpty()) return
        val hasFilter =
            fn.findDescendants<LookupCall>().any { !it.isScalar } ||
                fn.findDescendants<CollectionAccess>().isNotEmpty()
        if (!hasFilter) return
        for (call in bulkCalls) {
            findings.add(buildFinding(fn, call))
        }
    }

    private fun buildFinding(
        fn: FunctionDecl,
        call: FunctionCall,
    ): Finding {
        val cx = ComplexityModel.fullScanForLookup()
        val evidence =
            listOf(
                Evidence(
                    location = call.location,
                    label = "${call.name}() loads all records",
                    executionContext = ExecutionContext.SINGLE,
                    complexity = ComplexityModel.bottleneck("O(n)"),
                ),
                Evidence(
                    location = fn.location,
                    label = "followed by in-memory filtering in ${fn.name}()",
                    executionContext = ExecutionContext.SINGLE,
                    complexity = "O(n)",
                ),
            )
        return Finding(
            ruleId = id,
            ruleName = name,
            severity = severity,
            location = call.location,
            message = "${call.name}() followed by in-memory filtering in ${fn.name}()",
            suggestions = listOf(Suggestion.Freeform("Use a targeted query instead of loading all records and filtering in memory")),
            currentComplexity = cx.current,
            suggestedComplexity = cx.suggested,
            evidence = evidence,
        )
    }
}

internal fun isBulkLoadCall(
    call: FunctionCall,
    bulkLoadPrefixes: List<String>,
    domTargets: Set<String>,
    typeEnv: TypeEnvironment? = null,
): Boolean {
    if (!bulkLoadPrefixes.any { startsWithAtWordBoundary(call.name, it) }) return false
    // Exclude DOM/test framework targets (e.g., wrapper.findAll in Vue test utils). Original
    // case for the boundary check - "dom"/"el" are short enough that "random"/"freedom"/
    // "model"/"channel" all satisfied a bare contains with no boundary at all. A receiver
    // merely NAMED "wrapper"/"element" isn't necessarily one though - an OrderRepository
    // field someone happened to call "wrapper" would be wrongly excluded here. When the
    // declared type is known, check that instead of the bare variable name.
    val target = call.qualifiedTarget ?: return true
    val declaredType = typeEnv?.typeOf(target)?.simpleName
    if (containsAnyAtWordBoundary(declaredType ?: target, domTargets)) return false
    return true
}
