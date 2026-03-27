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
@Suppress("LargeClass") // Cohesive rule: scan + classify + build findings for one anti-pattern
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
                val classified = group.calls.map { it to classifyMutation(it, langOrJava, context.registry) }
                if (classified.any { it.second == MutationType.COLLECTION_EXPANSION }) {
                    findings.add(buildGroupedCartesianFinding(group, langOrJava, context.registry, classified))
                }
                // All mutations are scalar/string/keyed → suppress entirely
            }
            scanFlatMap(fileRoot, context.registry.streamEntryMethods(langOrJava), findings)
        }
        return findings
    }

    /** Classifies whether a mutation grows a collection (Cartesian risk) or merely accumulates/aggregates. */
    internal enum class MutationType(
        val label: String,
    ) {
        COLLECTION_EXPANSION("collection expansion"),
        SCALAR_ACCUMULATION("scalar"),
        STRING_BUILDING("string building"),
        KEYED_AGGREGATION("keyed aggregation"),
    }

    @Suppress("ReturnCount") // Guard-clause classification by method name and receiver context
    private fun classifyMutation(
        call: FunctionCall,
        language: Language,
        registry: LanguageSemanticsRegistry,
    ): MutationType {
        val methodName = call.name
        val target = call.qualifiedTarget?.lowercase() ?: ""

        // Unambiguous scalar methods (subtract, multiply, incrementAndGet, etc.)
        if (methodName in registry.scalarAccumulationMethods(language)) return MutationType.SCALAR_ACCUMULATION

        // Keyed aggregation (put, merge, compute, etc.) — always Map operations
        if (methodName in registry.keyedAggregationMethods(language)) return MutationType.KEYED_AGGREGATION

        // String building (append, concat) — these methods are inherently string operations
        // in Java/Kotlin/Groovy/JS. No collection type has an `append` or `concat` method.
        if (methodName in registry.stringBuildingMethods(language)) return MutationType.STRING_BUILDING

        // Ambiguous `add` — check if receiver looks like a scalar accumulator.
        // List.add() grows a collection, but BigDecimal.add() accumulates a scalar.
        // Use receiver name heuristics: scalar hint words, single-char variables,
        // and absence of collection-like naming patterns.
        if (methodName == "add") {
            val scalarHints = registry.scalarReceiverHints(language)
            if (scalarHints.any { target.contains(it) }) return MutationType.SCALAR_ACCUMULATION
            // Single-char variable names (w, x, n) are almost always scalars, never collections
            if (target.length == 1 && target[0].isLetter()) return MutationType.SCALAR_ACCUMULATION
        }

        return MutationType.COLLECTION_EXPANSION
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

    @Suppress("LongMethod") // Multi-step loop-pair classification with partitioned-iteration filtering
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

        // Constant-bound outer loop (enum, config list) → O(k*m) not O(n*m)
        if (outerLoop.isConstantBound) return
        // Inner loop exits after one iteration (break/throw/return) → output bounded by outer size
        if (innerLoop.isSingleIteration) return

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
        filterMethods: Set<String>,
    ): Confidence {
        val outerIdx = loopStack.indexOf(outerLoop)
        val innerIdx = loopStack.indexOf(innerLoop)
        if (innerIdx > outerIdx + 1) return Confidence.MEDIUM
        if (hasFilterBetween(outerLoop, innerLoop, filterMethods)) return Confidence.MEDIUM
        return Confidence.HIGH
    }

    private fun hasFilterBetween(
        outerLoop: LoopNode,
        innerLoop: LoopNode,
        filterMethods: Set<String>,
    ): Boolean {
        for (child in outerLoop.children) {
            if (child === innerLoop) return false
            if (child is FunctionCall && child.name in filterMethods) {
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
    @Suppress("ReturnCount") // Guard clauses with early returns — clearer than nested if/else
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
            @Suppress("MagicNumber") // 30-char threshold distinguishes loop-element names from qualified paths
            if (innerBase.length <= 30 && !innerBase.contains("(")) return Severity.INFO
        }
        return severity
    }

    @Suppress("LongMethod") // Assembles evidence chain, message, and complexity estimate in one place
    private fun buildGroupedCartesianFinding(
        group: MutationGroup,
        language: Language,
        registry: LanguageSemanticsRegistry,
        classified: List<Pair<FunctionCall, MutationType>> = emptyList(),
    ): Finding {
        val outerLoop = group.outerLoop
        val innerLoop = group.innerLoop
        val outerVar = outerLoop.iteratedVariable ?: ""
        val innerVar = innerLoop.iteratedVariable ?: ""
        val expansionCalls =
            if (classified.isNotEmpty()) {
                classified.filter { it.second == MutationType.COLLECTION_EXPANSION }.map { it.first }
            } else {
                group.calls
            }
        val firstCall = expansionCalls.minByOrNull { it.location.line } ?: group.calls.minBy { it.location.line }
        val isSameCollection = outerVar == innerVar

        val confidence =
            if (isSameCollection) {
                Confidence.LOW
            } else {
                determineConfidence(outerLoop, innerLoop, group.loopStack, registry.filterMethods(language))
            }
        val effectiveSeverity =
            if (isSameCollection) Severity.INFO else determineEffectiveSeverity(outerVar, innerVar, language, registry)

        val estimate = ComplexityModel.cartesianProduct(outerVar, innerVar)
        val mutationLabel = buildMutationLabel(expansionCalls)
        val evidence =
            listOf(
                Evidence(
                    outerLoop.location,
                    outerLoop.kind.label(),
                    ExecutionContext.INSIDE_LOOP,
                    complexity = "O($outerVar)",
                ),
                Evidence(
                    innerLoop.location,
                    innerLoop.kind.label(),
                    ExecutionContext.INSIDE_LOOP,
                    depth = 1,
                    complexity = "O($innerVar)",
                ),
                Evidence(
                    firstCall.location,
                    "$mutationLabel in nested loop",
                    ExecutionContext.INSIDE_LOOP,
                    depth = 2,
                    complexity =
                        ComplexityModel.bottleneck(
                            "O($outerVar \u00d7 $innerVar)",
                        ),
                ),
            )

        val nonExpandingNames =
            if (classified.isNotEmpty()) {
                classified
                    .filter { it.second != MutationType.COLLECTION_EXPANSION }
                    .map { "${it.first.name} (${it.second.label})" }
                    .distinct()
            } else {
                emptyList()
            }

        val baseMessage =
            if (expansionCalls.size == 1) {
                "Cartesian product: ${firstCall.name}() in nested loops " +
                    "over $outerVar \u00d7 $innerVar produces O(n \u00d7 m) results"
            } else {
                val distinctNames = expansionCalls.map { it.name }.distinct().sorted()
                "Cartesian product: ${expansionCalls.size} mutations (${distinctNames.joinToString(", ")}) " +
                    "in nested loops over $outerVar \u00d7 $innerVar produces O(n \u00d7 m) results"
            }
        val message =
            if (nonExpandingNames.isNotEmpty()) {
                "$baseMessage (${nonExpandingNames.joinToString(", ")} excluded — not collection growth)"
            } else {
                baseMessage
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
        streamEntryMethods: Set<String>,
        findings: MutableList<Finding>,
    ) {
        if (node is FunctionCall && node.name == "flatMap") {
            val sourceVar = node.qualifiedTarget
            val innerIteration = findInnerIteration(node.children, streamEntryMethods)
            if (sourceVar != null && innerIteration != null && sourceVar != innerIteration) {
                findings.add(buildFlatMapFinding(node, sourceVar, innerIteration))
            }
        }
        for (child in node.children) {
            scanFlatMap(child, streamEntryMethods, findings)
        }
    }

    private fun findInnerIteration(
        children: List<IRNode>,
        streamEntryMethods: Set<String>,
    ): String? {
        for (child in children) {
            if (child is FunctionCall && child.name in streamEntryMethods) {
                return child.qualifiedTarget
            }
            val found = findInnerIteration(child.children, streamEntryMethods)
            if (found != null) return found
        }
        return null
    }

    @Suppress("LongMethod") // Assembles evidence chain, message, and complexity estimate for flatMap pattern
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
                    complexity = "O($sourceVar)",
                ),
                Evidence(
                    call.location,
                    "inner stream over $innerVar",
                    ExecutionContext.INSIDE_LOOP,
                    depth = 1,
                    complexity =
                        ComplexityModel.bottleneck(
                            "O($sourceVar \u00d7 $innerVar)",
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
        val langOrJava = language ?: Language.JAVA
        val copyOnModify = context.registry.copyOnModifyMethodsFor(langOrJava)
        // Only include mutations that GROW a collection — exclude replacements,
        // removals, and in-place operations that don't produce Cartesian output
        val nonGrowth = context.registry.nonGrowthMutations(langOrJava)
        return (copyOnModify + context.registry.mutationMethods(langOrJava)) - nonGrowth
    }
}
