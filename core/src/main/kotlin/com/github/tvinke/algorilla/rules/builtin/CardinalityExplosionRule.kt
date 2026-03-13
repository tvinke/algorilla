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

    private fun determineEffectiveSeverity(
        outerVar: String,
        innerVar: String,
    ): Severity {
        val outerLower = outerVar.lowercase()
        val innerLower = innerVar.lowercase()
        if (SMALL_COLLECTION_HINTS.any { it in outerLower || it in innerLower }) {
            return Severity.INFO
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
        return copyOnModify + context.registry.allMutationMethods()
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
        private val SMALL_COLLECTION_HINTS =
            setOf(
                "type",
                "status",
                "enum",
                "config",
            )
    }
}
