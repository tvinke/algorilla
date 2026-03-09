package com.github.tvinke.algorilla.util

import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.IRNode

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
     */
    public fun resolve(
        call: FunctionCall,
        symbolTable: SymbolTable,
    ): FunctionDecl? {
        if (call.qualifiedTarget != null) {
            val qualified = symbolTable.lookup("${call.qualifiedTarget}.${call.name}")
            if (qualified.isNotEmpty()) return qualified.first()
        }
        val byName = symbolTable.lookupBySimpleName(call.name)
        return byName.firstOrNull()
    }

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
