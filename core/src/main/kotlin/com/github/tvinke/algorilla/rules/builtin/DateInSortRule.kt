package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.model.ExecutionContext
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.ObjectCreation
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.model.SortCall
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Evidence
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.Rule
import com.github.tvinke.algorilla.util.findDescendants

/**
 * Detects Date/LocalDate object creation inside sort comparators. Parsing or creating
 * date objects on every comparison adds unnecessary overhead to sorting.
 */
public class DateInSortRule : Rule {
    override val id: String = "date-in-sort"
    override val name: String = "Date In Sort"
    override val severity: Severity = Severity.WARNING
    override val languages: Set<Language> = Language.entries.toSet()

    override fun evaluate(context: AnalysisContext): List<Finding> {
        val findings = mutableListOf<Finding>()
        for ((_, fileRoot) in context.irTrees) {
            scanNode(fileRoot, findings)
        }
        return findings
    }

    private fun scanNode(
        node: IRNode,
        findings: MutableList<Finding>,
    ) {
        if (node is SortCall) {
            checkComparatorBody(node, findings)
        }
        for (child in node.children) {
            scanNode(child, findings)
        }
    }

    private fun checkComparatorBody(
        sort: SortCall,
        findings: MutableList<Finding>,
    ) {
        val body = sort.comparatorBody ?: return
        val creations = body.flatMap { it.findDescendants<ObjectCreation>() }
        val dateCreations = creations.filter { isDateType(it.typeName) }
        for (creation in dateCreations) {
            findings.add(buildFinding(sort, creation))
        }
    }

    private fun buildFinding(
        sort: SortCall,
        creation: ObjectCreation,
    ): Finding {
        val evidence =
            listOf(
                Evidence(
                    location = sort.location,
                    label = "${sort.kind.name.lowercase()} with comparator",
                    executionContext = ExecutionContext.SINGLE,
                ),
                Evidence(
                    location = creation.location,
                    label = "new ${creation.typeName}() inside comparator",
                    executionContext = ExecutionContext.INSIDE_LOOP,
                ),
            )
        return Finding(
            ruleId = id,
            ruleName = name,
            severity = severity,
            location = sort.location,
            message = "${creation.typeName} creation inside sort comparator",
            suggestion = "Compare ISO date strings directly, or parse dates once before sorting",
            currentComplexity = "O(n log n * parse)",
            suggestedComplexity = "O(n log n)",
            evidence = evidence,
        )
    }
}

internal val dateTypeNames: Set<String> =
    setOf(
        "Date",
        "LocalDate",
        "LocalDateTime",
        "ZonedDateTime",
        "Instant",
    )

internal fun isDateType(typeName: String): Boolean = dateTypeNames.any { typeName.contains(it) }
