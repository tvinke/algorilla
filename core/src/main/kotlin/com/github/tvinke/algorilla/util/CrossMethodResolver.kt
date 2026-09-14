package com.github.tvinke.algorilla.util

import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.model.Confidence
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.semantics.LanguageSemanticsRegistry

/**
 * The outcome of [CrossMethodResolver.resolve]: either a resolved [FunctionDecl] with a
 * [ResolutionConfidence] saying how sure that resolution is, or [Unresolved] when the symbol
 * table has nothing to offer at all. Replaces a plain nullable `FunctionDecl?` return, which
 * couldn't distinguish "resolved with confidence" from "resolved by an arbitrary pick among
 * equally-plausible overloads" - callers that care about that distinction (e.g. for confidence
 * scoring on a finding) previously had no way to see it.
 */
public sealed interface ResolutionResult {
    /**
     * A [FunctionDecl] was found for the call, with [confidence] saying whether that pick was
     * unambiguous ([ResolutionConfidence.EXACT]) or an arbitrary choice among several
     * equally-plausible overloads ([ResolutionConfidence.AMBIGUOUS_OVERLOAD_BEST_GUESS]).
     */
    public data class Resolved(
        val decl: FunctionDecl,
        val confidence: ResolutionConfidence,
    ) : ResolutionResult

    /** No candidate at all - the call was skipped, had no matching receiver, or no candidates existed. */
    public data object Unresolved : ResolutionResult
}

/**
 * How confident a [ResolutionResult.Resolved] pick is.
 */
public enum class ResolutionConfidence {
    /** Exactly one candidate existed, either from the start or after filtering by parameter count. */
    EXACT,

    /**
     * More than one equally-plausible candidate remained (same name, and either no candidate
     * matched the call's argument count, or several did) - the pick is [List.first] among
     * them, not a resolution backed by real disambiguation.
     */
    AMBIGUOUS_OVERLOAD_BEST_GUESS,
}

/**
 * Unwraps to the resolved [FunctionDecl], or null on [ResolutionResult.Unresolved] - for the
 * common case of callers that only need the declaration and don't care about
 * [ResolutionConfidence]. Callers that need the confidence too (e.g. for finding-confidence
 * scoring) should match on [ResolutionResult] directly instead.
 */
public fun ResolutionResult.declOrNull(): FunctionDecl? =
    when (this) {
        is ResolutionResult.Unresolved -> null
        is ResolutionResult.Resolved -> decl
    }

/**
 * Floors [computed] to [Confidence.LOW] when this is
 * [ResolutionConfidence.AMBIGUOUS_OVERLOAD_BEST_GUESS] - an ambiguous overload guess means the
 * resolved declaration might not be the one the call actually targets, so no rule built on top
 * of it should report higher than LOW, whatever its own signals say. Shared by the rules that
 * weigh resolution confidence into a finding's confidence, so the floor isn't re-implemented
 * per rule.
 */
public fun ResolutionConfidence.demoteIfAmbiguous(computed: Confidence): Confidence =
    if (this == ResolutionConfidence.AMBIGUOUS_OVERLOAD_BEST_GUESS) Confidence.LOW else computed

/**
 * The more pessimistic of two [ResolutionConfidence] values - [ResolutionConfidence.AMBIGUOUS_OVERLOAD_BEST_GUESS]
 * wins over [ResolutionConfidence.EXACT]. A multi-hop resolution chain (one call resolves to a
 * function whose body calls another function, which is itself resolved, and so on) is only as
 * trustworthy as its least certain hop - an exact outermost resolution doesn't redeem an
 * ambiguous one two calls deeper. Used to fold hop-by-hop confidence into a single value for
 * the whole chain rather than reporting only the first or last hop's confidence.
 */
public fun worstOf(
    a: ResolutionConfidence,
    b: ResolutionConfidence,
): ResolutionConfidence =
    if (a == ResolutionConfidence.AMBIGUOUS_OVERLOAD_BEST_GUESS || b == ResolutionConfidence.AMBIGUOUS_OVERLOAD_BEST_GUESS) {
        ResolutionConfidence.AMBIGUOUS_OVERLOAD_BEST_GUESS
    } else {
        ResolutionConfidence.EXACT
    }

/**
 * A [value] found via [CrossMethodResolver.resolveAndFindWithConfidence], paired with the worst
 * (most ambiguous) [ResolutionConfidence] seen at any hop of the resolution chain that found it.
 */
public data class ResolvedMatch<T>(
    val value: T,
    val confidence: ResolutionConfidence,
)

/**
 * Resolves a [FunctionCall] to its [FunctionDecl] using the symbol table, then checks
 * whether the resolved function body contains nodes matching a predicate.
 *
 * Useful for rules that need to look "one level deep" into called methods
 * (e.g. detecting date parsing inside a method referenced in a sort comparator).
 */
public object CrossMethodResolver {
    /**
     * Checks if the given function call resolves to a function whose body
     * contains any descendant matching [predicate].
     *
     * Edge case: when the match is found several hops deep (via [maxDepth] > 1), the
     * confidence of every intermediate hop is discarded - only the found node itself is
     * returned. Callers that need to know whether any hop along the way was an ambiguous
     * overload guess should use [resolveAndFindWithConfidence] instead.
     *
     * @param maxDepth how many levels of indirection to follow (default 1)
     * @return the first matching descendant node, or null
     */
    public inline fun <reified T : IRNode> resolveAndFind(
        call: FunctionCall,
        symbolTable: SymbolTable,
        maxDepth: Int = 1,
        language: Language = Language.JAVA,
        noinline predicate: (T) -> Boolean = { true },
    ): T? = resolveAndFindWithConfidence(call, symbolTable, maxDepth, language, predicate)?.value

    /**
     * Same as [resolveAndFind], but also reports the worst (most ambiguous)
     * [ResolutionConfidence] seen at any hop of the chain that led to the match - not just the
     * outermost call's own resolution. A caller that only re-resolves [call] itself to read
     * confidence would miss ambiguity introduced two or more hops deep; this doesn't.
     */
    public inline fun <reified T : IRNode> resolveAndFindWithConfidence(
        call: FunctionCall,
        symbolTable: SymbolTable,
        maxDepth: Int = 1,
        language: Language = Language.JAVA,
        noinline predicate: (T) -> Boolean = { true },
    ): ResolvedMatch<T>? = resolveAndFindWithConfidenceInternal(call, symbolTable, maxDepth, language, T::class.java, predicate)

    @PublishedApi
    internal fun <T : IRNode> resolveAndFindWithConfidenceInternal(
        call: FunctionCall,
        symbolTable: SymbolTable,
        maxDepth: Int,
        language: Language,
        targetClass: Class<T>,
        predicate: (T) -> Boolean,
    ): ResolvedMatch<T>? {
        if (maxDepth <= 0) return null
        val (resolved, confidence) =
            when (val result = resolve(call, symbolTable, language)) {
                is ResolutionResult.Unresolved -> return null
                is ResolutionResult.Resolved -> result.decl to result.confidence
            }

        // Search direct descendants
        val allDescendants = collectDescendants(resolved)

        @Suppress("UNCHECKED_CAST")
        val direct = allDescendants.filter { targetClass.isInstance(it) }.map { it as T }.firstOrNull(predicate)
        if (direct != null) return ResolvedMatch(direct, confidence)

        // Follow one more level
        if (maxDepth > 1) {
            for (innerCall in allDescendants.filterIsInstance<FunctionCall>()) {
                val result = resolveAndFindWithConfidenceInternal(innerCall, symbolTable, maxDepth - 1, language, targetClass, predicate)
                if (result != null) return ResolvedMatch(result.value, worstOf(confidence, result.confidence))
            }
        }
        return null
    }

    /**
     * Resolves a [FunctionCall] to its [FunctionDecl] via the symbol table.
     * Tries qualified target first, then falls back to simple name lookup.
     *
     * Precondition: [call] must not be a built-in stream/collection operation that's never a
     * user-defined method (`map`, `filter`, ...) - those are skipped up front, via the
     * unresolvable-names set from the semantics registry, before any lookup runs.
     *
     * Guarantee: when [call] has an explicit receiver (`qualifiedTarget` like "repository" or
     * "service"), resolution only ever goes through the qualified lookup path - it never falls
     * back to simple-name lookup, which would incorrectly match an unrelated local method of the
     * same name (e.g. `dataPointRepository.persist()` resolving to a local `persist()`).
     *
     * Edge case: when multiple overloads share the resolved name, the one whose parameter count
     * matches the call's argument count is preferred (see [bestMatch]); when even that leaves
     * more than one equally-plausible candidate, the pick among them is arbitrary rather than a
     * genuine resolution - reflected in the returned [ResolutionResult]'s
     * [ResolutionConfidence.AMBIGUOUS_OVERLOAD_BEST_GUESS], not silently hidden behind a
     * successful-looking [ResolutionResult.Resolved].
     *
     * Guarantee: the set of unresolvable names is derived from the semantics registry (YAML)
     * rather than hardcoded here, so it stays in sync with the method classification.
     */
    public fun resolve(
        call: FunctionCall,
        symbolTable: SymbolTable,
        language: Language = Language.JAVA,
        enclosingClass: String? = null,
    ): ResolutionResult {
        val skip = LanguageSemanticsRegistry.DEFAULT.unresolvableNames(language)
        if (call.name in skip) return ResolutionResult.Unresolved
        if (call.qualifiedTarget != null) {
            val byTarget = resolveByTarget(call, symbolTable)
            if (byTarget is ResolutionResult.Resolved) return byTarget
        }
        // Only fall back to simple name when there's no explicit receiver,
        // or receiver is this/super (which refers to the current class)
        if (call.qualifiedTarget != null && call.qualifiedTarget !in SELF_OR_SUPER_REFERENCES) {
            return ResolutionResult.Unresolved
        }
        val byName = symbolTable.lookupBySimpleName(call.name)
        // Prefer methods in the same declaring class to avoid name collisions
        // (e.g. merge() in 39 Mapper classes - pick the one in the caller's own class)
        if (enclosingClass != null) {
            val sameClass = byName.filter { it.declaringClass == enclosingClass }
            if (sameClass.isNotEmpty()) return bestMatch(sameClass, call)
        }
        return bestMatch(byName, call)
    }

    @Suppress("ReturnCount") // Guard clauses with early returns for each resolution strategy
    private fun resolveByTarget(
        call: FunctionCall,
        symbolTable: SymbolTable,
    ): ResolutionResult {
        val target = call.qualifiedTarget ?: return ResolutionResult.Unresolved
        val qualified = symbolTable.lookup("$target.${call.name}")
        if (qualified.isNotEmpty()) return bestMatch(qualified, call)
        val byClass = symbolTable.lookupByClassAndName("$target.${call.name}")
        if (byClass.isNotEmpty()) return bestMatch(byClass, call)
        val targetType = symbolTable.resolveType(target) ?: return ResolutionResult.Unresolved
        val byType = symbolTable.lookupByClassAndName("$targetType.${call.name}")
        if (byType.isNotEmpty()) return bestMatch(byType, call)
        // Interface→implementation: resolve via registered supertypes (single-impl only)
        for (implClass in symbolTable.implementationsOf(targetType)) {
            val byImpl = symbolTable.lookupByClassAndName("$implClass.${call.name}")
            if (byImpl.isNotEmpty()) return bestMatch(byImpl, call)
        }
        return ResolutionResult.Unresolved
    }

    /**
     * When multiple candidates match, prefer the one whose parameter count matches the call's
     * argument count. Determines [ResolutionConfidence]: a single candidate - either from the
     * start or after filtering by parameter count - is [ResolutionConfidence.EXACT]; falling
     * back to an arbitrary [List.first] pick, either among several same-arity candidates or
     * among the original candidates when none matched arity, is
     * [ResolutionConfidence.AMBIGUOUS_OVERLOAD_BEST_GUESS].
     */
    private fun bestMatch(
        candidates: List<FunctionDecl>,
        call: FunctionCall,
    ): ResolutionResult {
        if (candidates.isEmpty()) return ResolutionResult.Unresolved
        if (candidates.size == 1) {
            return ResolutionResult.Resolved(candidates.first(), ResolutionConfidence.EXACT)
        }
        val byParamCount = candidates.filter { it.parameters.size == call.arguments.size }
        val exactMatch = byParamCount.singleOrNull()
        if (exactMatch != null) return ResolutionResult.Resolved(exactMatch, ResolutionConfidence.EXACT)
        // Either no candidate matched the call's arity, or several equally-plausible ones did -
        // either way this is an arbitrary pick, not a genuine disambiguation.
        val guess = byParamCount.firstOrNull() ?: candidates.first()
        return ResolutionResult.Resolved(guess, ResolutionConfidence.AMBIGUOUS_OVERLOAD_BEST_GUESS)
    }

    /**
     * Receivers that refer to the current class hierarchy rather than to some other
     * object — an explicit `this` or `super`. Used wherever a call target needs to be
     * recognized as "within this class" for resolution or confidence purposes.
     *
     * Not the same concept as [com.github.tvinke.algorilla.util.isSelfCallOf]'s notion of a
     * self-call: that one deliberately excludes `super`, because `super.foo()` dispatches to
     * the superclass's implementation rather than repeating this method's own logic — see
     * its kdoc for the Broadleaf `super.getSectionKey()` case this distinction fixes.
     */
    public val SELF_OR_SUPER_REFERENCES: Set<String> = setOf("this", "super")

    private fun collectDescendants(node: IRNode): List<IRNode> {
        val results = mutableListOf<IRNode>()
        val stack = ArrayDeque<IRNode>()
        stack.addAll(node.children)
        while (stack.isNotEmpty()) {
            val current = stack.removeFirst()
            results.add(current)
            for (child in current.children.asReversed()) {
                stack.addFirst(child)
            }
        }
        return results
    }
}
