package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.model.Confidence
import com.github.tvinke.algorilla.model.ExecutionContext
import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.GenericNode
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LoopNode
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.model.VariableDecl
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.ComplexityModel
import com.github.tvinke.algorilla.rules.Evidence
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.Rule
import com.github.tvinke.algorilla.rules.RuleCategory
import com.github.tvinke.algorilla.rules.Suggestion
import com.github.tvinke.algorilla.semantics.LanguageSemanticsRegistry
import com.github.tvinke.algorilla.semantics.TypeEnvironment
import com.github.tvinke.algorilla.util.containsAnyAtWordBoundary
import com.github.tvinke.algorilla.util.findDescendants
import com.github.tvinke.algorilla.util.referencesName
import com.github.tvinke.algorilla.util.resolveInitializer
import com.github.tvinke.algorilla.util.startsWithAtWordBoundary

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
@Suppress("LargeClass", "TooManyFunctions") // Cohesive rule: scan + classify + build findings for one anti-pattern
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
            scanNode(fileRoot, null, emptyList(), emptyList(), mutationMethods, langOrJava, context.registry, mutationGroups)
            for ((_, group) in mutationGroups) {
                val typeEnv = group.enclosingFn?.let { context.typeEnvironmentFor(it) }
                val classified = group.calls.map { it to classifyMutation(it, langOrJava, context.registry, typeEnv) }
                if (classified.any { it.second == MutationType.COLLECTION_EXPANSION }) {
                    findings.add(buildGroupedCartesianFinding(group, langOrJava, context.registry, classified))
                }
                // All mutations are scalar/string/keyed → suppress entirely
            }
            scanFlatMap(
                fileRoot,
                null,
                emptyList(),
                context.registry.streamEntryMethods(langOrJava),
                langOrJava,
                context.registry,
                findings,
            )
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
        typeEnv: TypeEnvironment?,
    ): MutationType {
        val methodName = call.name
        // Original case - lowercasing first would destroy the camelCase signal the
        // scalarHints boundary check below needs.
        val target = call.qualifiedTarget ?: ""

        // Unambiguous scalar methods (subtract, multiply, incrementAndGet, etc.)
        if (methodName in registry.scalarAccumulationMethods(language)) return MutationType.SCALAR_ACCUMULATION

        // Keyed aggregation (put, merge, compute, etc.) — always Map operations
        if (methodName in registry.keyedAggregationMethods(language)) return MutationType.KEYED_AGGREGATION

        // String building (append, concat) — these methods are inherently string operations
        // in Java/Kotlin/Groovy/JS. No collection type has an `append` or `concat` method.
        if (methodName in registry.stringBuildingMethods(language)) return MutationType.STRING_BUILDING

        // Ambiguous `add` — check if receiver looks like a scalar accumulator.
        // List.add() grows a collection, but BigDecimal.add() accumulates a scalar.
        if (methodName == "add") {
            // A declared type overrides the name hint either way — see classifyByDeclaredType below.
            // Only when the type is unresolved do we fall back to name heuristics, same as
            // before that check existed.
            classifyByDeclaredType(typeEnv, target)?.let { return it }
            // Use receiver name heuristics: scalar hint words, single-char variables,
            // and absence of collection-like naming patterns.
            val scalarHints = registry.scalarReceiverHints(language)
            // "sum"/"count"/"cost"/"amount" are short enough that "consumer"/"discount"/
            // "costume"/"paramount" all satisfied a bare contains with no boundary at all.
            if (containsAnyAtWordBoundary(target, scalarHints)) return MutationType.SCALAR_ACCUMULATION
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
        val enclosingFn: FunctionDecl?,
        val calls: MutableList<FunctionCall> = mutableListOf(),
    )

    @Suppress("LongParameterList") // Threading the enclosing function (and its var-decl scope) through
    private fun scanNode(
        node: IRNode,
        enclosingFn: FunctionDecl?,
        scope: List<VariableDecl>,
        loopStack: List<LoopNode>,
        mutationMethods: Set<String>,
        language: Language,
        registry: LanguageSemanticsRegistry,
        mutationGroups: MutableMap<LoopPairKey, MutationGroup>,
    ) {
        val fn = if (node is FunctionDecl) node else enclosingFn
        // Computed once per function, not once per mutation call inside it - findDescendants
        // walks the whole function body, and a function can contain many mutation calls
        // sharing the same nested loops.
        val fnScope = if (node is FunctionDecl) node.findDescendants<VariableDecl>() else scope

        if (node is LoopNode) {
            for (child in node.children) {
                scanNode(child, fn, fnScope, loopStack + node, mutationMethods, language, registry, mutationGroups)
            }
            return
        }

        if (loopStack.size >= 2 && node is FunctionCall && node.name in mutationMethods) {
            collectCartesianProduct(node, fn, fnScope, loopStack, language, registry, mutationGroups)
        }

        for (child in node.children) {
            scanNode(child, fn, fnScope, loopStack, mutationMethods, language, registry, mutationGroups)
        }
    }

    @Suppress("LongParameterList") // Threading the enclosing function/scope/loop context through
    private fun collectCartesianProduct(
        call: FunctionCall,
        enclosingFn: FunctionDecl?,
        scope: List<VariableDecl>,
        loopStack: List<LoopNode>,
        language: Language,
        registry: LanguageSemanticsRegistry,
        mutationGroups: MutableMap<LoopPairKey, MutationGroup>,
    ) {
        val outerLoop = loopStack[loopStack.size - 2]
        val innerLoop = loopStack.last()
        val outerVar = outerLoop.iteratedVariable ?: return
        val innerVar = innerLoop.iteratedVariable ?: return
        if (!isEligibleCartesianPair(outerLoop, innerLoop, outerVar, innerVar, language, registry, scope)) return

        val key = LoopPairKey(outerLoop.location.line, innerLoop.location.line)
        mutationGroups
            .getOrPut(key) {
                MutationGroup(outerLoop, innerLoop, loopStack.toList(), enclosingFn)
            }.calls
            .add(call)
    }

    /**
     * True when [outerLoop]/[innerLoop] form a genuine Cartesian-product risk worth recording -
     * i.e. none of the known "not actually O(n×m)" shapes apply: a constant-bound outer loop,
     * an inner loop that exits after one iteration, or (when the two loops iterate different
     * collections) a partitioned/derived-per-element inner collection, naming-based or
     * redeclaration-based.
     */
    @Suppress("LongParameterList", "ReturnCount") // Guard clauses with early returns — clearer than nested if/else
    private fun isEligibleCartesianPair(
        outerLoop: LoopNode,
        innerLoop: LoopNode,
        outerVar: String,
        innerVar: String,
        language: Language,
        registry: LanguageSemanticsRegistry,
        scope: List<VariableDecl>,
    ): Boolean {
        // Constant-bound outer loop (enum, config list) → O(k*m) not O(n*m)
        if (outerLoop.isConstantBound) return false
        // Inner loop exits after one iteration (break/throw/return) → output bounded by outer size
        if (innerLoop.isSingleIteration) return false

        if (outerVar != innerVar) {
            if (isPartitionedIteration(outerVar, innerVar, language, registry, scope)) return false
            if (isRederivedPerOuterIteration(outerVar, innerVar, outerLoop, innerLoop, scope)) return false
        }
        return true
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
     *
     * [innerVar] (and, for the structural entrySet/keySet check in case 1 only, [outerVar])
     * is resolved through [resolveExpressionText] first, so a value copied into a local
     * before the loop (`var values = entry.getValue(); for (v : values)`) is recognized from
     * what it was actually assigned, not from the copy's bare name — a local named `values`
     * isn't a map's values just because it looks like one. Case 2's outer/inner name match is
     * deliberately compared against [outerVar] unresolved: it's a naming convention between
     * two independently-chosen identifiers ("departments" / "department"), not a structural
     * check of what the collection actually is — resolving `departments` to its initializer
     * (`service.getDepartments()`) would compare against the unrelated receiver `service`
     * instead of the collection's own descriptive name, breaking a match that has nothing to
     * do with where the collection came from.
     */
    @Suppress("ReturnCount") // Guard clauses with early returns — clearer than nested if/else
    private fun isPartitionedIteration(
        outerVar: String,
        innerVar: String,
        language: Language,
        registry: LanguageSemanticsRegistry,
        scope: List<VariableDecl>,
    ): Boolean {
        val resolvedInnerVar = resolveExpressionText(innerVar, scope)

        // Case 1: Map entry unpacking — outer is entrySet()/keySet(), inner accesses values.
        // endsWith, not contains: a bare `contains` on the unanchored "values" entry matched
        // "values" buried mid-identifier ("entry.getMetaValues()", "row.valuesCache" both
        // contain "values" with no boundary at all - the same shape this whole batch fixes
        // elsewhere), silently treating an unrelated getter as map-entry-value access. Every
        // entry (".getValue()"/".values"/".value"/"values") already reads naturally as "the
        // call/property this string names, in full" - endsWith enforces exactly that,
        // without needing containsAtWordBoundary (which would still match "getMetaValues()"
        // too, since its capitalized "Values" satisfies a real camelCase boundary the same
        // way "ResultSet" does elsewhere in this campaign).
        val resolvedOuterVar = resolveExpressionText(outerVar, scope)
        val outerIsEntrySet = resolvedOuterVar.endsWith(".entrySet()") || resolvedOuterVar.endsWith(".keySet()")
        if (outerIsEntrySet && registry.mapValueAccessors(language).any { resolvedInnerVar.endsWith(it) }) return true

        // Case 2: Inner iterates a property/method of the outer loop element.
        // The inner variable has a dotted path (method call on an element), suggesting it
        // iterates a child collection OF the outer element — O(sum of children), not O(product).
        // e.g., outer="this.relations", inner="relationship.getKeyMaps()"
        //       outer="departments", inner="department.getEmployees()"
        //       outer="clazz.getInterfaces()", inner="ifc.getMethods()"
        val innerBase = resolvedInnerVar.substringBefore(".")
        if (innerBase.isNotEmpty() && innerBase != resolvedInnerVar) {
            // Inner has a dotted path — it's calling a method on an element variable.
            // Check if the base could be the loop element from the outer collection.
            // Uses the original outerVar, not a resolved one - see the class doc above.
            val outerBase = outerVar.substringBefore(".")
            val outerClean = outerBase.trimEnd('s', 'S')
            // Match: outer="departments" → outerClean="department", inner starts with "department"
            if (outerClean.isNotEmpty() && matchesElementName(innerBase, outerClean)) return true
            // Match: outer collection has no plural suffix but inner base is a plausible element name.
            // If the outer collection is a method call like "getInterfaces()", the element is often
            // a shortened name like "ifc" — we can't match that. But if the inner is a getter
            // call on a single-word variable, demote to INFO instead of suppressing entirely.
        }

        return false
    }

    /**
     * Detects the case where the inner loop's source collection is (re-)declared inside the
     * outer loop's own body — i.e. before the inner loop starts, on every outer iteration —
     * from a call that's actually derived FROM the outer element, not merely a same-named local
     * rebound to something unrelated. Such a collection can't be the same instance across outer
     * iterations, so growing it produces O(sum of per-iteration sizes) — a flatten/partition,
     * not a Cartesian product.
     *
     * Covers cases `isPartitionedIteration` misses because the naming heuristic doesn't apply:
     * a per-locale `Set` rebuilt each iteration (ConceptValidator), or a query result re-fetched
     * fresh per outer element (DatabaseUpdater's `changeSets = getChangeSets(fileName)`).
     *
     * Deliberately requires the redeclaration's initializer to reference the outer loop's own
     * per-element variable (`getNames(locale)`) rather than just matching on position and name -
     * a same-named local re-fetched from a source that has nothing to do with the outer element
     * (`values = otherService.fetchAll()`, returning the same full result every iteration) is
     * still a genuine Cartesian product, just recomputed instead of cached. The parser doesn't
     * expose the per-element loop variable itself (only [LoopNode.iteratedVariable], the
     * collection being iterated) as a language-independent IR field, so - same as
     * [isPartitionedIteration]'s case 2 - it's derived from [outerVar] by de-pluralizing:
     * "locales" → "locale". Same known limitation as that sibling check: a collection name
     * with no trailing 's' (`clientList`) won't match its element name (`client`).
     */
    private fun isRederivedPerOuterIteration(
        outerVar: String,
        innerVar: String,
        outerLoop: LoopNode,
        innerLoop: LoopNode,
        scope: List<VariableDecl>,
    ): Boolean {
        val outerElementVar = outerVar.substringBefore(".").trimEnd('s', 'S')
        if (outerElementVar.isEmpty()) return false
        val redeclaration =
            scope.firstOrNull { decl ->
                decl.name == innerVar &&
                    decl.location.line > outerLoop.location.line &&
                    decl.location.line < innerLoop.location.line
            } ?: return false
        // Not redeclaration.initializer: the Kotlin parser never populates that field (it puts
        // the initializer expression in children instead), so relying on it silently loses this
        // check for Kotlin sources. Java populates both with the same content - children works
        // for both.
        val initializer = redeclaration.children.filterIsInstance<FunctionCall>().firstOrNull() ?: return false
        return initializer.qualifiedTarget == outerElementVar ||
            initializer.arguments.any {
                it.referencesName(outerElementVar) || (it is GenericNode && it.nodeType == outerElementVar)
            }
    }

    private fun determineEffectiveSeverity(
        outerVar: String,
        innerVar: String,
        language: Language,
        registry: LanguageSemanticsRegistry,
    ): Severity {
        // Original case for the boundary check - lowercasing first would destroy the
        // camelCase signal. "type" is short enough that "prototype"/"genotype"/"stereotype"/
        // "phenotype"/"archetype" all satisfied it with no boundary at all, demoting a real
        // Cartesian-product finding to INFO on an unrelated pair of variables.
        val hints = registry.smallCollectionHints(language)
        if (containsAnyAtWordBoundary(outerVar, hints) || containsAnyAtWordBoundary(innerVar, hints)) {
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

    @Suppress("LongParameterList") // Threading enclosingFn/scope/language/registry through the recursive walk
    private fun scanFlatMap(
        node: IRNode,
        enclosingFn: FunctionDecl?,
        scope: List<VariableDecl>,
        streamEntryMethods: Set<String>,
        language: Language,
        registry: LanguageSemanticsRegistry,
        findings: MutableList<Finding>,
    ) {
        // Scoped per enclosing function, not per file - same reasoning as scanNode's fnScope:
        // a bare variable name should only resolve against declarations that could actually be
        // in scope at the flatMap call site, not a same-named local from an unrelated function.
        val fnScope = if (node is FunctionDecl) node.findDescendants<VariableDecl>() else scope

        if (node is FunctionCall && node.name == "flatMap") {
            val sourceVar = node.qualifiedTarget
            val innerIteration = findInnerIteration(node.children, streamEntryMethods)
            if (sourceVar != null &&
                innerIteration != null &&
                isCrossCollectionFlatMap(sourceVar, innerIteration, language, registry, fnScope)
            ) {
                findings.add(buildFlatMapFinding(node, sourceVar, innerIteration))
            }
        }
        val fn = if (node is FunctionDecl) node else enclosingFn
        for (child in node.children) {
            scanFlatMap(child, fn, fnScope, streamEntryMethods, language, registry, findings)
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

/**
 * Resolves [varName] to the expression it was actually assigned from, or returns [varName]
 * unchanged when it isn't a bare local copy of some other call — either because it's already
 * a dotted expression (e.g. `entry.getValue()`, which is never itself a [VariableDecl] name),
 * or because [scope] has no declaration for it at all (a parameter, or a name from outside
 * the resolvable scope).
 *
 * This is the piece that closes the gap [resolveInitializer] alone leaves open: a bare
 * variable name only tells you what something is *called*, not what it *is*. Reconstructing
 * `"receiver.method()"` from the resolved [FunctionCall] lets every downstream check keep
 * working against the same shape of text it already expects, whether or not a copy sits
 * between the loop/call site and the value's real origin.
 */
private fun resolveExpressionText(
    varName: String,
    scope: List<VariableDecl>,
): String {
    val initializer = resolveInitializer(varName, scope) ?: return varName
    val target = initializer.qualifiedTarget ?: return varName
    return "$target.${initializer.name}()"
}

/**
 * True when a `flatMap` over [sourceVar] whose lambda iterates [innerIteration] is a genuine
 * cross join: the two sides name different collections, and [sourceVar] doesn't resolve to an
 * `Optional`/`Mono`-shaped source (cardinality ≤ 1, via [LanguageSemanticsRegistry.isMonadicTarget])
 * that could never produce an O(n × m) result in the first place. Resolves [sourceVar] through
 * [resolveExpressionText] first, so a source copied into a local before the `flatMap` call
 * (`var maybe = repo.findOptional(); maybe.flatMap(...)`) is judged by what it was actually
 * assigned, not by its bare name.
 */
private fun isCrossCollectionFlatMap(
    sourceVar: String,
    innerIteration: String,
    language: Language,
    registry: LanguageSemanticsRegistry,
    scope: List<VariableDecl>,
): Boolean = sourceVar != innerIteration && !registry.isMonadicTarget(language, resolveExpressionText(sourceVar, scope))

/**
 * A real collection type (`List<BigDecimal> totalAmounts`) is never a scalar just because
 * it's named like one, and a real O(1)/String/other non-collection type definitely isn't a
 * collection expansion either — a declared type wins over the name heuristic either way.
 * Returns null when the receiver's type can't be resolved, so the caller falls back to the
 * name-based heuristics.
 */
private fun classifyByDeclaredType(
    typeEnv: TypeEnvironment?,
    target: String,
): CardinalityExplosionRule.MutationType? {
    // declaredTypeName (not the raw typeOf) - a NAME_HEURISTIC-sourced guess (e.g. "results"
    // inferred as "List" purely from an initializer call named getOrderList()) is exactly
    // the kind of weak evidence this whole check exists to NOT trust; falling through to the
    // name heuristics below is the correct behavior for that case, not isCollection's
    // filtered-to-false verdict on it.
    if (typeEnv?.declaredTypeName(target) == null) return null
    return if (typeEnv.isCollection(target)) {
        CardinalityExplosionRule.MutationType.COLLECTION_EXPANSION
    } else {
        CardinalityExplosionRule.MutationType.SCALAR_ACCUMULATION
    }
}

/**
 * Returns true if [innerBase] is exactly [outerClean] (ignoring case), or extends it at
 * a real camelCase word boundary — "departmentHead" for "department" — but not a bare
 * text prefix like "career"/"cargo" for "car". Plain `startsWith` had no such boundary:
 * de-pluralizing "cars" to "car" then matched any inner name starting with "car",
 * silently suppressing a genuine Cartesian product over unrelated collections. Delegates
 * to the shared [startsWithAtWordBoundary] rather than reimplementing the same boundary
 * check locally.
 */
private fun matchesElementName(
    innerBase: String,
    outerClean: String,
): Boolean = startsWithAtWordBoundary(innerBase, outerClean)
