package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.model.Confidence
import com.github.tvinke.algorilla.model.ExecutionContext
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LookupCall
import com.github.tvinke.algorilla.model.ObjectCreation
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.model.SortCall
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.ComplexityModel
import com.github.tvinke.algorilla.rules.Evidence
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.Rule
import com.github.tvinke.algorilla.rules.RuleCategory
import com.github.tvinke.algorilla.rules.Suggestion
import com.github.tvinke.algorilla.semantics.LanguageSemanticsRegistry
import com.github.tvinke.algorilla.util.CrossMethodResolver
import com.github.tvinke.algorilla.util.findDescendants

/**
 * Detects expensive operations inside sort comparators: linear lookups, date parsing/creation,
 * and heavyweight object construction. When a comparator performs O(n) work per comparison,
 * the overall sort becomes O(n^2 log n) instead of O(n log n).
 *
 * Also follows method references via cross-method resolution to catch indirect patterns.
 */
@Suppress("LargeClass")
public class ExpensiveSortComparatorRule : Rule {
    override val id: String = "expensive-sort-comparator"
    override val name: String = "Expensive Sort Comparator"
    override val severity: Severity = Severity.WARNING
    override val languages: Set<Language> = Language.entries.toSet()
    override val category: RuleCategory = RuleCategory.SORT_ABUSE
    override val defaultConfidence: Confidence = Confidence.HIGH
    override val aliases: List<String> = listOf("date-in-sort")

    override fun evaluate(context: AnalysisContext): List<Finding> {
        val findings = mutableListOf<Finding>()
        for ((_, fileRoot) in context.irTrees) {
            scanNode(fileRoot, fileRoot.language, context, findings)
        }
        return findings
    }

    private fun scanNode(
        node: IRNode,
        language: Language,
        context: AnalysisContext,
        findings: MutableList<Finding>,
    ) {
        if (node is SortCall) {
            checkComparatorBody(node, language, context, findings)
        }
        for (child in node.children) {
            scanNode(child, language, context, findings)
        }
    }

    private fun checkComparatorBody(
        sort: SortCall,
        language: Language,
        context: AnalysisContext,
        findings: MutableList<Finding>,
    ) {
        val body = sort.comparatorBody ?: return

        // Linear lookups inside comparator
        val lookups = body.filterIsInstance<LookupCall>() + body.flatMap { it.findDescendants<LookupCall>() }
        for (lookup in lookups.filter { !it.isO1 && !it.isScalar }) {
            findings.add(buildLookupFinding(sort, lookup))
        }

        // Date creation inside comparator
        val creations = body.filterIsInstance<ObjectCreation>() + body.flatMap { it.findDescendants<ObjectCreation>() }
        val dateCreations = creations.filter { isDateType(it.typeName, language, context.registry) }
        for (creation in dateCreations) {
            findings.add(buildDateCreationFinding(sort, creation))
        }

        // Date parse calls inside comparator
        val calls = body.filterIsInstance<FunctionCall>() + body.flatMap { it.findDescendants<FunctionCall>() }
        val dateParseCalls = calls.filter { isDateParseCall(it, language, context.registry) }
        for (call in dateParseCalls) {
            findings.add(buildDateParseFinding(sort, call))
        }

        // Cross-method: check called methods for hidden date operations
        val nonDateCalls = calls.filter { !isDateParseCall(it, language, context.registry) }
        for (call in nonDateCalls) {
            checkCrossMethodDate(sort, call, language, context, findings)
        }
    }

    private fun checkCrossMethodDate(
        sort: SortCall,
        call: FunctionCall,
        language: Language,
        context: AnalysisContext,
        findings: MutableList<Finding>,
    ) {
        val maxDepth = context.config.maxCallDepth.coerceAtMost(2)
        val dateOp =
            CrossMethodResolver.resolveAndFind<ObjectCreation>(
                call,
                context.symbolTable,
                maxDepth = maxDepth,
            ) { isDateType(it.typeName, language, context.registry) }
        if (dateOp != null) {
            findings.add(buildIndirectFinding(sort, call, dateOp))
            return
        }
        val parseOp =
            CrossMethodResolver.resolveAndFind<FunctionCall>(
                call,
                context.symbolTable,
                maxDepth = maxDepth,
            ) { isDateParseCall(it, language, context.registry) }
        if (parseOp != null) {
            findings.add(buildIndirectFinding(sort, call, parseOp))
        }
    }

    private fun buildLookupFinding(
        sort: SortCall,
        lookup: LookupCall,
    ): Finding {
        val targetVar = lookup.targetVariable ?: "collection"
        val cx = ComplexityModel.sortTimesLookup()
        val innerEvidence =
            Evidence(
                lookup.location,
                "linear ${lookup.kind.label} on '$targetVar' inside comparator",
                ExecutionContext.INSIDE_LOOP,
                depth = 1,
                complexity = ComplexityModel.bottleneck("O(n)"),
            )
        return buildFinding(
            sort,
            lookup.location,
            cx,
            innerEvidence,
            "Linear lookup on '$targetVar' inside sort comparator",
            "Build a lookup Map from '$targetVar' before the sort",
        )
    }

    private fun buildDateCreationFinding(
        sort: SortCall,
        creation: ObjectCreation,
    ): Finding {
        val cx = ComplexityModel.sortTimesParse()
        val innerEvidence =
            Evidence(
                creation.location,
                "new ${creation.typeName}() inside comparator",
                ExecutionContext.INSIDE_LOOP,
                depth = 1,
                complexity = ComplexityModel.bottleneck("parse"),
            )
        return buildFinding(
            sort,
            creation.location,
            cx,
            innerEvidence,
            "${creation.typeName} creation inside sort comparator",
            "Compare ISO date strings directly, or parse dates once before sorting",
        )
    }

    private fun buildDateParseFinding(
        sort: SortCall,
        call: FunctionCall,
    ): Finding {
        val cx = ComplexityModel.sortTimesParse()
        val target = call.qualifiedTarget ?: "Date"
        val innerEvidence =
            Evidence(
                call.location,
                "$target.${call.name}() inside comparator",
                ExecutionContext.INSIDE_LOOP,
                depth = 1,
                complexity = ComplexityModel.bottleneck("parse"),
            )
        return buildFinding(
            sort,
            call.location,
            cx,
            innerEvidence,
            "Date parsing (${call.name}) inside sort comparator",
            "Parse dates once before sorting into a Map, then compare pre-parsed values",
        )
    }

    private fun buildIndirectFinding(
        sort: SortCall,
        call: FunctionCall,
        innerNode: IRNode,
    ): Finding {
        val cx = ComplexityModel.sortTimesParse()
        val desc =
            when (innerNode) {
                is ObjectCreation -> "new ${innerNode.typeName}()"
                is FunctionCall -> "${innerNode.qualifiedTarget ?: ""}.${innerNode.name}()"
                else -> "expensive operation"
            }
        val evidence =
            listOf(
                sortEvidence(sort),
                Evidence(call.location, "${call.name}() called from comparator", ExecutionContext.INSIDE_LOOP, depth = 1),
                Evidence(
                    innerNode.location,
                    "$desc inside ${call.name}()",
                    ExecutionContext.INSIDE_LOOP,
                    depth = 2,
                    complexity = ComplexityModel.bottleneck("parse"),
                ),
            )
        return buildFinding(
            innerNode.location,
            cx,
            evidence,
            "Date operation inside ${call.name}() called from sort comparator",
            "Parse dates once before sorting, not inside the comparator call chain",
        )
    }

    private fun sortEvidence(sort: SortCall) =
        Evidence(
            sort.location,
            "${sort.kind.label} with comparator",
            ExecutionContext.SINGLE,
            complexity = ComplexityModel.sortEvidence(),
        )

    private fun buildFinding(
        sort: SortCall,
        location: com.github.tvinke.algorilla.model.SourceLocation,
        cx: com.github.tvinke.algorilla.rules.ComplexityEstimate,
        innerEvidence: Evidence,
        message: String,
        suggestion: String,
    ) = Finding(
        ruleId = id,
        ruleName = name,
        severity = severity,
        location = location,
        message = message,
        suggestions = listOf(Suggestion.Freeform(suggestion)),
        currentComplexity = cx.current,
        suggestedComplexity = cx.suggested,
        evidence = listOf(sortEvidence(sort), innerEvidence),
    )

    private fun buildFinding(
        location: com.github.tvinke.algorilla.model.SourceLocation,
        cx: com.github.tvinke.algorilla.rules.ComplexityEstimate,
        evidence: List<Evidence>,
        message: String,
        suggestion: String,
    ) = Finding(
        ruleId = id,
        ruleName = name,
        severity = severity,
        location = location,
        message = message,
        suggestions = listOf(Suggestion.Freeform(suggestion)),
        currentComplexity = cx.current,
        suggestedComplexity = cx.suggested,
        evidence = evidence,
    )
}

internal fun isDateType(
    typeName: String,
    language: Language,
    registry: LanguageSemanticsRegistry,
): Boolean = registry.dateTypeNames(language).any { typeName.contains(it) }

internal fun isDateParseCall(
    call: FunctionCall,
    language: Language,
    registry: LanguageSemanticsRegistry,
): Boolean {
    if (call.name !in registry.dateParseMethods(language)) return false
    val target = call.qualifiedTarget ?: return false
    return registry.dateParseTargets(language).any { target.contains(it) }
}
