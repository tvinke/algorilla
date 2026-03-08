package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.model.ExecutionContext
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.ObjectCreation
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.model.SortCall
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Evidence
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.Rule
import com.github.tvinke.algorilla.util.CrossMethodResolver
import com.github.tvinke.algorilla.util.findDescendants

/**
 * Detects Date/LocalDate object creation or date parsing inside sort comparators.
 * Parsing or creating date objects on every comparison adds unnecessary overhead to sorting.
 *
 * Also follows method references: if a comparator calls a helper method that internally
 * creates/parses dates, this rule catches it via cross-method resolution.
 */
public class DateInSortRule : Rule {
    override val id: String = "date-in-sort"
    override val name: String = "Date In Sort"
    override val severity: Severity = Severity.WARNING
    override val languages: Set<Language> = Language.entries.toSet()

    override fun evaluate(context: AnalysisContext): List<Finding> {
        val findings = mutableListOf<Finding>()
        for ((_, fileRoot) in context.irTrees) {
            scanNode(fileRoot, context, findings)
        }
        return findings
    }

    private fun scanNode(
        node: IRNode,
        context: AnalysisContext,
        findings: MutableList<Finding>,
    ) {
        if (node is SortCall) {
            checkComparatorBody(node, context, findings)
        }
        for (child in node.children) {
            scanNode(child, context, findings)
        }
    }

    private fun checkComparatorBody(
        sort: SortCall,
        context: AnalysisContext,
        findings: MutableList<Finding>,
    ) {
        val body = sort.comparatorBody ?: return

        // Direct date object creation
        val creations = body.filterIsInstance<ObjectCreation>() + body.flatMap { it.findDescendants<ObjectCreation>() }
        val dateCreations = creations.filter { isDateType(it.typeName) }
        for (creation in dateCreations) {
            findings.add(buildFindingForCreation(sort, creation))
        }

        // Direct date parse calls (LocalDate.parse(), DateTimeFormatter.parse(), etc.)
        val calls = body.filterIsInstance<FunctionCall>() + body.flatMap { it.findDescendants<FunctionCall>() }
        val dateParseCalls = calls.filter { isDateParseCall(it) }
        for (call in dateParseCalls) {
            findings.add(buildFindingForParseCall(sort, call))
        }

        // Cross-method: check if any called method internally does date operations
        val otherCalls = calls.filter { !isDateParseCall(it) }
        for (call in otherCalls) {
            val dateOp = CrossMethodResolver.resolveAndFind<ObjectCreation>(
                call, context.symbolTable, maxDepth = context.config.maxCallDepth.coerceAtMost(2),
            ) { isDateType(it.typeName) }
            if (dateOp != null) {
                findings.add(buildFindingForIndirect(sort, call, dateOp))
                continue
            }
            val parseOp = CrossMethodResolver.resolveAndFind<FunctionCall>(
                call, context.symbolTable, maxDepth = context.config.maxCallDepth.coerceAtMost(2),
            ) { isDateParseCall(it) }
            if (parseOp != null) {
                findings.add(buildFindingForIndirect(sort, call, parseOp))
            }
        }
    }

    private fun buildFindingForCreation(sort: SortCall, creation: ObjectCreation): Finding {
        val evidence = listOf(
            Evidence(sort.location, "${sort.kind.name.lowercase()} with comparator", ExecutionContext.SINGLE),
            Evidence(creation.location, "new ${creation.typeName}() inside comparator", ExecutionContext.INSIDE_LOOP),
        )
        return Finding(
            ruleId = id, ruleName = name, severity = severity,
            location = sort.location,
            message = "${creation.typeName} creation inside sort comparator",
            suggestion = "Compare ISO date strings directly, or parse dates once before sorting",
            currentComplexity = "O(n log n * parse)", suggestedComplexity = "O(n log n)",
            evidence = evidence,
        )
    }

    private fun buildFindingForParseCall(sort: SortCall, call: FunctionCall): Finding {
        val target = call.qualifiedTarget ?: "Date"
        val evidence = listOf(
            Evidence(sort.location, "${sort.kind.name.lowercase()} with comparator", ExecutionContext.SINGLE),
            Evidence(call.location, "$target.${call.name}() inside comparator", ExecutionContext.INSIDE_LOOP),
        )
        return Finding(
            ruleId = id, ruleName = name, severity = severity,
            location = sort.location,
            message = "Date parsing (${call.name}) inside sort comparator",
            suggestion = "Parse dates once before sorting into a Map, then compare pre-parsed values",
            currentComplexity = "O(n log n * parse)", suggestedComplexity = "O(n log n)",
            evidence = evidence,
        )
    }

    private fun buildFindingForIndirect(sort: SortCall, call: FunctionCall, innerNode: IRNode): Finding {
        val desc = when (innerNode) {
            is ObjectCreation -> "new ${innerNode.typeName}()"
            is FunctionCall -> "${innerNode.qualifiedTarget ?: ""}.${innerNode.name}()"
            else -> "date operation"
        }
        val evidence = listOf(
            Evidence(sort.location, "${sort.kind.name.lowercase()} with comparator", ExecutionContext.SINGLE),
            Evidence(call.location, "${call.name}() called from comparator", ExecutionContext.INSIDE_LOOP),
            Evidence(innerNode.location, "$desc inside ${call.name}()", ExecutionContext.INSIDE_LOOP),
        )
        return Finding(
            ruleId = id, ruleName = name, severity = severity,
            location = sort.location,
            message = "Date operation inside ${call.name}() called from sort comparator",
            suggestion = "Parse dates once before sorting, not inside the comparator call chain",
            currentComplexity = "O(n log n * parse)", suggestedComplexity = "O(n log n)",
            evidence = evidence,
        )
    }
}

internal val dateTypeNames: Set<String> =
    setOf("Date", "LocalDate", "LocalDateTime", "ZonedDateTime", "Instant", "OffsetDateTime")

internal fun isDateType(typeName: String): Boolean = dateTypeNames.any { typeName.contains(it) }

private val DATE_PARSE_METHOD_NAMES = setOf("parse", "from", "ofEpochMilli", "ofEpochSecond")

private val DATE_PARSE_TARGETS = setOf(
    "LocalDate", "LocalDateTime", "ZonedDateTime", "Instant",
    "OffsetDateTime", "DateTimeFormatter", "SimpleDateFormat",
)

private fun isDateParseCall(call: FunctionCall): Boolean {
    if (call.name !in DATE_PARSE_METHOD_NAMES) return false
    val target = call.qualifiedTarget ?: return false
    return DATE_PARSE_TARGETS.any { target.contains(it) }
}
