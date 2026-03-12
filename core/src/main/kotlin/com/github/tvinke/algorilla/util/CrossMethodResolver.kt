package com.github.tvinke.algorilla.util

import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.semantics.LanguageSemanticsRegistry

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
     * @param maxDepth how many levels of indirection to follow (default 1)
     * @return the first matching descendant node, or null
     */
    public inline fun <reified T : IRNode> resolveAndFind(
        call: FunctionCall,
        symbolTable: SymbolTable,
        maxDepth: Int = 1,
        noinline predicate: (T) -> Boolean = { true },
    ): T? = resolveAndFindInternal(call, symbolTable, maxDepth, T::class.java, predicate)

    @PublishedApi
    internal fun <T : IRNode> resolveAndFindInternal(
        call: FunctionCall,
        symbolTable: SymbolTable,
        maxDepth: Int,
        targetClass: Class<T>,
        predicate: (T) -> Boolean,
    ): T? {
        if (maxDepth <= 0) return null
        val resolved = resolve(call, symbolTable) ?: return null

        // Search direct descendants
        val allDescendants = collectDescendants(resolved)

        @Suppress("UNCHECKED_CAST")
        val direct = allDescendants.filter { targetClass.isInstance(it) }.map { it as T }.firstOrNull(predicate)
        if (direct != null) return direct

        // Follow one more level
        if (maxDepth > 1) {
            for (innerCall in allDescendants.filterIsInstance<FunctionCall>()) {
                val result = resolveAndFindInternal(innerCall, symbolTable, maxDepth - 1, targetClass, predicate)
                if (result != null) return result
            }
        }
        return null
    }

    /**
     * Resolves a [FunctionCall] to its [FunctionDecl] via the symbol table.
     * Tries qualified target first, then falls back to simple name lookup.
     * Skips built-in stream/collection operations that are never user-defined methods.
     *
     * When the call has an explicit receiver (qualifiedTarget like "repository" or "service"),
     * we only resolve via qualified lookup — falling back to simple name would incorrectly
     * match local methods (e.g. `dataPointRepository.persist()` resolving to local `persist()`).
     *
     * When multiple overloads exist, prefers the one whose parameter count matches
     * the call's argument count.
     *
     * The set of unresolvable names is derived from the semantics registry (YAML),
     * ensuring it stays in sync with the method classification.
     */
    public fun resolve(
        call: FunctionCall,
        symbolTable: SymbolTable,
    ): FunctionDecl? {
        if (call.name in unresolvableNames) return null
        if (call.qualifiedTarget != null) {
            val byTarget = resolveByTarget(call, symbolTable)
            if (byTarget != null) return byTarget
        }
        // Only fall back to simple name when there's no explicit receiver,
        // or receiver is this/super (which refers to the current class)
        if (call.qualifiedTarget != null && call.qualifiedTarget !in SELF_REFERENCES) {
            return null
        }
        val byName = symbolTable.lookupBySimpleName(call.name)
        return bestMatch(byName, call)
    }

    private fun resolveByTarget(
        call: FunctionCall,
        symbolTable: SymbolTable,
    ): FunctionDecl? {
        val target = call.qualifiedTarget ?: return null
        val qualified = symbolTable.lookup("$target.${call.name}")
        if (qualified.isNotEmpty()) return bestMatch(qualified, call)
        val byClass = symbolTable.lookupByClassAndName("$target.${call.name}")
        if (byClass.isNotEmpty()) return bestMatch(byClass, call)
        val targetType = symbolTable.resolveType(target) ?: return null
        val byType = symbolTable.lookupByClassAndName("$targetType.${call.name}")
        return if (byType.isNotEmpty()) bestMatch(byType, call) else null
    }

    /**
     * When multiple candidates match, prefer the one whose parameter count
     * matches the call's argument count.
     */
    private fun bestMatch(
        candidates: List<FunctionDecl>,
        call: FunctionCall,
    ): FunctionDecl? {
        if (candidates.isEmpty()) return null
        if (candidates.size == 1) return candidates.first()
        val byParamCount = candidates.filter { it.parameters.size == call.arguments.size }
        if (byParamCount.isNotEmpty()) return byParamCount.first()
        return candidates.first()
    }

    private val SELF_REFERENCES = setOf("this", "super")

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

    /**
     * Derived from the semantics registry: all method names that should not be resolved
     * to user-defined functions. Includes stream ops, scope functions, and all classified
     * collection methods (lookup, sort, access, iteration, etc.).
     */
    private val unresolvableNames: Set<String> by lazy {
        LanguageSemanticsRegistry.loadDefaults().allUnresolvableNames()
    }
}
