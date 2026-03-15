package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.model.Confidence
import com.github.tvinke.algorilla.model.ExecutionContext
import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LoopNode
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.ComplexityModel
import com.github.tvinke.algorilla.rules.Evidence
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.Rule
import com.github.tvinke.algorilla.rules.RuleCategory
import com.github.tvinke.algorilla.rules.Suggestion
import com.github.tvinke.algorilla.semantics.LanguageSemanticsRegistry

/**
 * Detects cardinality explosion patterns where the output grows as the
 * PRODUCT of input sizes rather than the sum:
 *
 * - **Pattern A:** Nested loops iterating DIFFERENT collections with a
 *   mutation call in the inner body (Cartesian product).
 * - **Pattern B:** `flatMap` whose lambda iterates a DIFFERENT collection
 *   than the flatMap source (stream-based cross join).
 *
 * Subsumes [InLoopCollectionBuildingRule] when both fire at the same
 * location, because this rule provides a more specific diagnosis
 * (cross-product vs. generic in-loop mutation).
 */
@Suppress("LargeClass")
public class CardinalityExplosionRule : Rule {
    override val id: String = "cardinality-explosion"
    override val name: String = "Cardinality Explosion"
    override val severity: Severity = Severity.WARNING
    override val defaultConfidence: Confidence = Confidence.MEDIUM
    override val languages: Set<Language> = Language.entries.toSet()
    override val category: RuleCategory = RuleCategory.LOOP_AMPLIFIER
    override val subsumes: Set<String> = setOf("in-loop-collection-building")

    override fun evaluate(context: AnalysisContext): List<Finding> {
        val findings = mutableListOf<Finding>()
        for ((_, fileRoot) in context.irTrees) {
            val language = (fileRoot as? FileRoot)?.language
            val mutationMethods = resolveMutationMethods(context, language)
            val langOrJava = language ?: Language.JAVA
            val mutationGroups = mutableMapOf<LoopPairKey, MutationGroup>()
            scanNode(fileRoot, emptyList(), mutationMethods, langOrJava, context.registry, mutationGroups)
            for ((_, group) in mutationGroups) {
                findings.add(buildGroupedCartesianFinding(group, langOrJava, context.registry))
            }
            scanFlatMap(fileRoot, findings)
        }
        return findings
    }

    // ── Pattern A: nested-loop Cartesian product ────────────────

    private data class LoopPairKey(
        val outerLine: Int,
        val innerLine: Int,
    )

    private data class MutationGroup(
        val outerLoop: LoopNode,
        val innerLoop: LoopNode,
        val loopStack: List<LoopNode>,
        val calls: MutableList<FunctionCall> = mutableListOf(),
    )

    private fun scanNode(
        node: IRNode,
        loopStack: List<LoopNode>,
        mutationMethods: Set<String>,
        language: Language,
        registry: LanguageSemanticsRegistry,
        mutationGroups: MutableMap<LoopPairKey, MutationGroup>,
    ) {
        if (node is LoopNode) {
            for (child in node.children) {
                scanNode(child, loopStack + node, mutationMethods, language, registry, mutationGroups)
            }
            return
        }

        if (loopStack.size >= 2 && node is FunctionCall && node.name in mutationMethods) {
            collectCartesianProduct(node, loopStack, language, registry, mutationGroups)
        }

        for (child in node.children) {
            scanNode(child, loopStack, mutationMethods, language, registry, mutationGroups)
        }
    }

    @Suppress("LongMethod")
    private fun collectCartesianProduct(
        call: FunctionCall,
        loopStack: List<LoopNode>,
        language: Language,
        registry: LanguageSemanticsRegistry,
        mutationGroups: MutableMap<LoopPairKey, MutationGroup>,
    ) {
        val outerLoop = loopStack[loopStack.size - 2]
        val innerLoop = loopStack.last()
        val outerVar = outerLoop.iteratedVariable ?: return
        val innerVar = innerLoop.iteratedVariable ?: return

        if (outerVar != innerVar) {
            if (isPartitionedIteration(outerVar, innerVar, language, registry)) return
        }

        val key = LoopPairKey(outerLoop.location.line, innerLoop.location.line)
        mutationGroups
            .getOrPut(key) {
                MutationGroup(outerLoop, innerLoop, loopStack.toList())
            }.calls
            .add(call)
    }

    private fun determineConfidence(
        outerLoop: LoopNode,
        innerLoop: LoopNode,
        loopStack: List<LoopNode>,
    ): Confidence {
        val outerIdx = loopStack.indexOf(outerLoop)
        val innerIdx = loopStack.indexOf(innerLoop)
        if (innerIdx > outerIdx + 1) return Confidence.MEDIUM
        if (hasFilterBetween(outerLoop, innerLoop)) return Confidence.MEDIUM
        return Confidence.HIGH
    }

    private fun hasFilterBetween(
        outerLoop: LoopNode,
        innerLoop: LoopNode,
    ): Boolean {
        for (child in outerLoop.children) {
            if (child === innerLoop) return false
            if (child is FunctionCall && child.name in FILTER_METHODS) {
                return true
            }
        }
        return false
    }

    /**
     * Detects partitioned iteration where inner collection is derived from outer element.
     * Total work is O(sum of parts), not O(outer × max_parts).
     *
     * Covers:
     * - Map entry unpacking: `map.entrySet()` → `entry.getValue()`
     * - Parent-child: `nodes` → `node.getChildren()`
     * - Enum values: `MyEnum.values()` → `type.getSubtypes()`
     */
    @Suppress("ReturnCount")
    private fun isPartitionedIteration(
        outerVar: String,
        innerVar: String,
        language: Language,
        registry: LanguageSemanticsRegistry,
    ): Boolean {
        // Case 1: Map entry unpacking — outer is entrySet()/keySet(), inner accesses values
        val outerIsEntrySet = outerVar.endsWith(".entrySet()") || outerVar.endsWith(".keySet()")
        if (outerIsEntrySet && registry.mapValueAccessors(language).any { innerVar.contains(it) }) return true

        // Case 2: Inner iterates a property/method of the outer loop element.
        // The inner variable has a dotted path (method call on an element), suggesting it
        // iterates a child collection OF the outer element — O(sum of children), not O(product).
        // e.g., outer="this.relations", inner="relationship.getKeyMaps()"
        //       outer="departments", inner="department.getEmployees()"
        //       outer="clazz.getInterfaces()", inner="ifc.getMethods()"
        val innerBase = innerVar.substringBefore(".")
        if (innerBase.isNotEmpty() && innerBase != innerVar) {
            // Inner has a dotted path — it's calling a method on an element variable.
            // Check if the base could be the loop element from the outer collection.
            val outerBase = outerVar.substringBefore(".")
            val outerClean = outerBase.trimEnd('s', 'S')
            // Match: outer="departments" → outerClean="department", inner starts with "department"
            if (outerClean.isNotEmpty() && innerBase.startsWith(outerClean, ignoreCase = true)) return true
            // Match: outer collection has no plural suffix but inner base is a plausible element name.
            // If the outer collection is a method call like "getInterfaces()", the element is often
            // a shortened name like "ifc" — we can't match that. But if the inner is a getter
            // call on a single-word variable, demote to INFO instead of suppressing entirely.
        }

        // Case 3: Enum/constant iteration — outer is Type.values() (uppercase initial)
        if (outerVar.endsWith(".values()")) {
            val typePrefix = outerVar.substringBefore(".values()")
            if (typePrefix.isNotEmpty() && typePrefix[0].isUpperCase()) return true
        }

        return false
    }

    private fun determineEffectiveSeverity(
        outerVar: String,
        innerVar: String,
        language: Language,
        registry: LanguageSemanticsRegistry,
    ): Severity {
        val outerLower = outerVar.lowercase()
        val innerLower = innerVar.lowercase()
        if (registry.smallCollectionHints(language).any { it in outerLower || it in innerLower }) {
            return Severity.INFO
        }
        // Inner is a method call on an element variable (e.g., "ifc.getMethods()", "node.getChildren()").
        // This is likely a child-accessor pattern — O(sum), not O(product). Demote to INFO.
        if (innerVar.contains(".") && innerVar.contains("(")) {
            val innerBase = innerVar.substringBefore(".")
            // Only demote if the base is a short variable name (loop element), not a qualified path
            @Suppress("MagicNumber")
            if (innerBase.length <= 30 && !innerBase.contains("(")) return Severity.INFO
        }
        return severity
    }

    @Suppress("LongMethod")
    private fun buildGroupedCartesianFinding(
        group: MutationGroup,
        language: Language,
        registry: LanguageSemanticsRegistry,
    ): Finding {
        val outerLoop = group.outerLoop
        val innerLoop = group.innerLoop
        val outerVar = outerLoop.iteratedVariable ?: ""
        val innerVar = innerLoop.iteratedVariable ?: ""
        val firstCall = group.calls.minBy { it.location.line }
        val isSameCollection = outerVar == innerVar

        val confidence =
            if (isSameCollection) Confidence.LOW else determineConfidence(outerLoop, innerLoop, group.loopStack)
        val effectiveSeverity =
            if (isSameCollection) Severity.INFO else determineEffectiveSeverity(outerVar, innerVar, language, registry)

        val estimate = ComplexityModel.cartesianProduct(outerVar, innerVar)
        val mutationLabel = buildMutationLabel(group.calls)
        val evidence =
            listOf(
                Evidence(
                    outerLoop.location,
                    outerLoop.kind.label(),
                    ExecutionContext.INSIDE_LOOP,
                    complexity = "O(|$outerVar|)",
                ),
                Evidence(
                    innerLoop.location,
                    innerLoop.kind.label(),
                    ExecutionContext.INSIDE_LOOP,
                    depth = 1,
                    complexity = "O(|$innerVar|)",
                ),
                Evidence(
                    firstCall.location,
                    "$mutationLabel in nested loop",
                    ExecutionContext.INSIDE_LOOP,
                    depth = 2,
                    complexity =
                        ComplexityModel.bottleneck(
                            "O(|$outerVar| \u00d7 |$innerVar|)",
                        ),
                ),
            )
        val message =
            if (group.calls.size == 1) {
                "Cartesian product: ${firstCall.name}() in nested loops " +
                    "over $outerVar \u00d7 $innerVar produces O(n \u00d7 m) results"
            } else {
                val distinctNames =
                    group.calls
                        .map { it.name }
                        .distinct()
                        .sorted()
                "Cartesian product: ${group.calls.size} mutations (${distinctNames.joinToString(", ")}) " +
                    "in nested loops over $outerVar \u00d7 $innerVar produces O(n \u00d7 m) results"
            }
        return Finding(
            ruleId = id,
            ruleName = name,
            severity = effectiveSeverity,
            confidence = confidence,
            location = firstCall.location,
            message = message,
            suggestions =
                listOf(
                    Suggestion.Freeform(
                        "Use an index or join strategy to avoid the full " +
                            "cross product \u2014 filter early or use a Map keyed on join attributes",
                    ),
                ),
            currentComplexity = estimate.current,
            suggestedComplexity = estimate.suggested,
            evidence = evidence,
        )
    }

    private fun buildMutationLabel(calls: List<FunctionCall>): String =
        if (calls.size == 1) {
            "${calls.first().name}()"
        } else {
            val distinctNames = calls.map { it.name }.distinct().sorted()
            "${calls.size} mutations (${distinctNames.joinToString(", ")})"
        }

    // ── Pattern B: flatMap explosion ────────────────────────────

    private fun scanFlatMap(
        node: IRNode,
        findings: MutableList<Finding>,
    ) {
        if (node is FunctionCall && node.name == "flatMap") {
            val sourceVar = node.qualifiedTarget
            val innerIteration = findInnerIteration(node.children)
            if (sourceVar != null && innerIteration != null && sourceVar != innerIteration) {
                findings.add(buildFlatMapFinding(node, sourceVar, innerIteration))
            }
        }
        for (child in node.children) {
            scanFlatMap(child, findings)
        }
    }

    private fun findInnerIteration(children: List<IRNode>): String? {
        for (child in children) {
            if (child is FunctionCall && child.name in STREAM_METHODS) {
                return child.qualifiedTarget
            }
            val found = findInnerIteration(child.children)
            if (found != null) return found
        }
        return null
    }

    @Suppress("LongMethod")
    private fun buildFlatMapFinding(
        call: FunctionCall,
        sourceVar: String,
        innerVar: String,
    ): Finding {
        val estimate = ComplexityModel.cartesianProduct(sourceVar, innerVar)
        val evidence =
            listOf(
                Evidence(
                    call.location,
                    "flatMap over $sourceVar",
                    ExecutionContext.INSIDE_LOOP,
                    complexity = "O(|$sourceVar|)",
                ),
                Evidence(
                    call.location,
                    "inner stream over $innerVar",
                    ExecutionContext.INSIDE_LOOP,
                    depth = 1,
                    complexity =
                        ComplexityModel.bottleneck(
                            "O(|$sourceVar| \u00d7 |$innerVar|)",
                        ),
                ),
            )
        return Finding(
            ruleId = id,
            ruleName = name,
            severity = severity,
            confidence = Confidence.HIGH,
            location = call.location,
            message =
                "flatMap cross join: flatMap over $sourceVar " +
                    "iterates $innerVar, producing O(n \u00d7 m) results",
            suggestions =
                listOf(
                    Suggestion.Freeform(
                        "Use an index or join strategy instead of " +
                            "streaming the full Cartesian product",
                    ),
                ),
            currentComplexity = estimate.current,
            suggestedComplexity = estimate.suggested,
            evidence = evidence,
        )
    }

    private fun resolveMutationMethods(
        context: AnalysisContext,
        language: Language?,
    ): Set<String> {
        val copyOnModify =
            if (language != null) {
                context.registry.copyOnModifyMethodsFor(language)
            } else {
                context.registry.allCopyOnModifyMethods()
            }
        // Only include mutations that GROW a collection — exclude replacements,
        // removals, and in-place operations that don't produce Cartesian output
        val nonGrowth =
            if (language != null) {
                context.registry.nonGrowthMutations(language)
            } else {
                context.registry.allExtraSection("non-growth-mutations")
            }
        return (copyOnModify + context.registry.allMutationMethods()) - nonGrowth
    }

    private companion object {
        private val FILTER_METHODS =
            setOf(
                "filter",
                "where",
                "takeIf",
                "filterNot",
                "removeIf",
            )
        private val STREAM_METHODS =
            setOf(
                "stream",
                "parallelStream",
                "iterator",
                "asSequence",
            )
    }
}
