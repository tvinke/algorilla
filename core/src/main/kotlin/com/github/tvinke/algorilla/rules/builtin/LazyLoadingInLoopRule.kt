package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.model.Confidence
import com.github.tvinke.algorilla.model.ExecutionContext
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
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
import com.github.tvinke.algorilla.util.findDescendants

/**
 * Detects potential JPA/Hibernate lazy-loading N+1 patterns: entity getter calls
 * inside loops where the getter name suggests a lazy-loaded collection.
 *
 * Heuristic approach (no type info available):
 * 1. Find variables assigned from repository-like fetch calls (findAll, getAll, etc.)
 * 2. Inside loops iterating those entities, detect getter calls with plural/collection names
 *
 * Conservative: only flags when the getter name strongly suggests a collection
 * (plural, ends with known collection suffixes). Defaults to LOW confidence since
 * we can't distinguish entity.getName() (scalar) from entity.getOrders() (lazy collection)
 * without type information.
 */
public class LazyLoadingInLoopRule : Rule {
    override val id: String = "lazy-loading-in-loop"
    override val name: String = "Lazy Loading In Loop"
    override val severity: Severity = Severity.WARNING
    override val languages: Set<Language> = setOf(Language.JAVA, Language.KOTLIN, Language.GROOVY)
    override val category: RuleCategory = RuleCategory.QUERY_PATTERN
    override val defaultConfidence: Confidence = Confidence.LOW
    override val requiresTypeContext: Boolean = true

    override fun evaluate(context: AnalysisContext): List<Finding> {
        val findings = mutableListOf<Finding>()
        for ((_, fileRoot) in context.irTrees) {
            val repoPatterns = context.registry.repositoryPatterns(fileRoot.language)
            val fetchPrefixes = context.registry.bulkLoadPrefixes(fileRoot.language)
            val scalarSuffs = context.registry.scalarSuffixes(fileRoot.language)
            val collGetterNames = context.registry.collectionGetterNames(fileRoot.language)
            for (fn in fileRoot.findDescendants<FunctionDecl>()) {
                checkFunction(fn, repoPatterns, fetchPrefixes, scalarSuffs, collGetterNames, context, findings)
            }
        }
        return findings
    }

    // Multi-step heuristic: find entity vars → scan loops → check getters; params pre-resolved for perf
    @Suppress("ReturnCount", "UnusedParameter", "LoopWithTooManyJumpStatements", "LongParameterList")
    private fun checkFunction(
        fn: FunctionDecl,
        repoPatterns: Set<String>,
        fetchPrefixes: List<String>,
        scalarSuffs: Set<String>,
        collGetterNames: Set<String>,
        context: AnalysisContext,
        findings: MutableList<Finding>,
    ) {
        // Step 1: find variables assigned from repository-like fetches
        val entityVars = findEntityVariables(fn, repoPatterns, fetchPrefixes)
        if (entityVars.isEmpty()) return

        // Step 2: find loops and check for collection-getter calls on entity variables
        for (loop in fn.findDescendants<LoopNode>()) {
            val loopVar = loop.iteratedVariable ?: continue
            // Check if the loop iterates a collection of entities (variable from a findAll/getAll)
            // OR if the loop variable itself was fetched from a repository
            val entityContext = entityVars.any { it == loopVar || isIteratingEntityCollection(loop, entityVars) }
            if (!entityContext) continue

            for (call in loop.findDescendants<FunctionCall>()) {
                if (isLazyCollectionGetter(call, scalarSuffs, collGetterNames) && isCalledOnLoopEntity(call, loop)) {
                    findings.add(buildFinding(call, loop))
                }
            }
        }
    }

    /**
     * Finds variable names assigned from repository-like fetch calls.
     */
    private fun findEntityVariables(
        fn: FunctionDecl,
        repoPatterns: Set<String>,
        fetchPrefixes: List<String>,
    ): Set<String> {
        val vars = mutableSetOf<String>()
        for (varDecl in fn.findDescendants<VariableDecl>()) {
            val initCalls = varDecl.findDescendants<FunctionCall>()
            if (initCalls.any { isRepositoryFetch(it, repoPatterns, fetchPrefixes) }) {
                vars.add(varDecl.name)
            }
        }
        return vars
    }

    private fun isRepositoryFetch(
        call: FunctionCall,
        repoPatterns: Set<String>,
        fetchPrefixes: List<String>,
    ): Boolean {
        val target = call.qualifiedTarget?.lowercase() ?: return false
        val isRepoTarget = repoPatterns.any { target.contains(it) }
        if (!isRepoTarget) return false
        return fetchPrefixes.any { call.name.startsWith(it, ignoreCase = true) }
    }

    private fun isIteratingEntityCollection(
        loop: LoopNode,
        entityVars: Set<String>,
    ): Boolean = loop.iteratedVariable in entityVars

    /**
     * Checks if a getter call suggests a lazy-loaded collection.
     * Conservative: only plural names or known collection-returning patterns.
     */
    private fun isLazyCollectionGetter(
        call: FunctionCall,
        scalarSuffs: Set<String>,
        collGetterNames: Set<String>,
    ): Boolean {
        val name = call.name
        // Must be a getter-style call
        if (!name.startsWith("get") || name.length <= MIN_GETTER_LENGTH) return false
        val property = name.removePrefix("get")
        val lower = property.lowercase()
        // Strong signals: plural property names that suggest collections
        return lower.endsWith("s") &&
            !scalarSuffs.any { lower.endsWith(it) } ||
            collGetterNames.any { lower.contains(it) }
    }

    private fun isCalledOnLoopEntity(
        call: FunctionCall,
        loop: LoopNode,
    ): Boolean {
        // The getter should be called on a variable that's the loop element
        // In for-each, the loop element is NOT the iteratedVariable (that's the collection)
        // The element variable isn't directly available, but the getter's qualifiedTarget
        // should NOT be the collection name itself
        val target = call.qualifiedTarget ?: return false
        return target != loop.iteratedVariable
    }

    @Suppress("LongMethod") // Assembles JPA-specific finding with lazy-load evidence chain
    private fun buildFinding(
        call: FunctionCall,
        loop: LoopNode,
    ): Finding {
        val loopVar = loop.iteratedVariable ?: "items"
        val target = call.qualifiedTarget ?: "entity"
        return Finding(
            ruleId = id,
            ruleName = name,
            severity = severity,
            confidence = Confidence.LOW,
            location = call.location,
            message =
                "$target.${call.name}() inside ${loop.kind.label()} may trigger lazy loading " +
                    "\u2014 N+1 query on each iteration",
            suggestions =
                listOf(
                    Suggestion.Freeform(
                        "Use a fetch join or @EntityGraph to eagerly load the association, " +
                            "or batch with IN clause",
                    ),
                ),
            currentComplexity = ComplexityModel.nPlusOne(loopVar).current,
            suggestedComplexity = ComplexityModel.nPlusOne(loopVar).suggested,
            evidence =
                listOf(
                    Evidence(
                        loop.location,
                        loop.kind.label(),
                        ExecutionContext.INSIDE_LOOP,
                        complexity = "O(|$loopVar|)",
                    ),
                    Evidence(
                        call.location,
                        "$target.${call.name}() — potential lazy load",
                        ExecutionContext.INSIDE_LOOP,
                        depth = 1,
                        complexity = "IO \u2190 bottleneck",
                    ),
                ),
        )
    }

    private companion object {
        private const val MIN_GETTER_LENGTH = 3
    }
}
