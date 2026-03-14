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
import com.github.tvinke.algorilla.semantics.LanguageSemanticsRegistry

private val smallCollectionHints: Set<String> by lazy {
    LanguageSemanticsRegistry.DEFAULT.allExtraSection("small-collection-hints")
}

private val mapValueAccessors: Set<String> by lazy {
    LanguageSemanticsRegistry.DEFAULT.allExtraSection("map-value-accessors")
}

private val nonGrowthMutations: Set<String> by lazy {
    LanguageSemanticsRegistry.DEFAULT.allExtraSection("non-growth-mutations")
}

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
            scanNode(fileRoot, emptyList(), mutationMethods, findings)
            scanFlatMap(fileRoot, findings)
        }
        return findings
    }

    // ── Pattern A: nested-loop Cartesian product ────────────────

    private fun scanNode(
        node: IRNode,
        loopStack: List<LoopNode>,
        mutationMethods: Set<String>,
        findings: MutableList<Finding>,
    ) {
        if (node is LoopNode) {
            for (child in node.children) {
                scanNode(child, loopStack + node, mutationMethods, findings)
            }
            return
        }

        if (loopStack.size >= 2 && node is FunctionCall && node.name in mutationMethods) {
            checkCartesianProduct(node, loopStack, findings)
        }

        for (child in node.children) {
            scanNode(child, loopStack, mutationMethods, findings)
        }
    }

    @Suppress("LongMethod")
    private fun checkCartesianProduct(
        call: FunctionCall,
        loopStack: List<LoopNode>,
        findings: MutableList<Finding>,
    ) {
        val outerLoop = loopStack[loopStack.size - 2]
        val innerLoop = loopStack.last()
        val outerVar = outerLoop.iteratedVariable ?: return
        val innerVar = innerLoop.iteratedVariable ?: return

        if (outerVar != innerVar) {
            // Partitioned iteration: inner iterates a property of the outer element.
            // Total work is O(sum of parts), not O(outer × max_parts).
            if (isPartitionedIteration(outerVar, innerVar)) return

            val confidence = determineConfidence(outerLoop, innerLoop, loopStack)
            val effectiveSeverity = determineEffectiveSeverity(outerVar, innerVar)
            findings.add(
                buildCartesianFinding(
                    call,
                    outerLoop,
                    innerLoop,
                    outerVar,
                    innerVar,
                    confidence,
                    effectiveSeverity,
                ),
            )
        } else {
            // Same collection — self-join, low confidence
            findings.add(
                buildCartesianFinding(
                    call,
                    outerLoop,
                    innerLoop,
                    outerVar,
                    innerVar,
                    Confidence.LOW,
                    Severity.INFO,
                ),
            )
        }
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
    ): Boolean {
        // Case 1: Map entry unpacking — outer is entrySet()/keySet(), inner accesses values
        val outerIsEntrySet = outerVar.endsWith(".entrySet()") || outerVar.endsWith(".keySet()")
        if (outerIsEntrySet && mapValueAccessors.any { innerVar.contains(it) }) return true

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
    ): Severity {
        val outerLower = outerVar.lowercase()
        val innerLower = innerVar.lowercase()
        if (smallCollectionHints.any { it in outerLower || it in innerLower }) {
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

    @Suppress("LongParameterList", "LongMethod")
    private fun buildCartesianFinding(
        call: FunctionCall,
        outerLoop: LoopNode,
        innerLoop: LoopNode,
        outerVar: String,
        innerVar: String,
        confidence: Confidence,
        effectiveSeverity: Severity,
    ): Finding {
        val estimate = ComplexityModel.cartesianProduct(outerVar, innerVar)
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
                    call.location,
                    "${call.name}() in nested loop",
                    ExecutionContext.INSIDE_LOOP,
                    depth = 2,
                    complexity =
                        ComplexityModel.bottleneck(
                            "O(|$outerVar| \u00d7 |$innerVar|)",
                        ),
                ),
            )
        return Finding(
            ruleId = id,
            ruleName = name,
            severity = effectiveSeverity,
            confidence = confidence,
            location = call.location,
            message =
                "Cartesian product: ${call.name}() in nested loops " +
                    "over $outerVar \u00d7 $innerVar produces O(n \u00d7 m) results",
            suggestion =
                "Use an index or join strategy to avoid the full " +
                    "cross product \u2014 filter early or use a Map keyed on join attributes",
            currentComplexity = estimate.current,
            suggestedComplexity = estimate.suggested,
            evidence = evidence,
        )
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
            suggestion =
                "Use an index or join strategy instead of " +
                    "streaming the full Cartesian product",
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
        return (copyOnModify + context.registry.allMutationMethods()) - nonGrowthMutations
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
