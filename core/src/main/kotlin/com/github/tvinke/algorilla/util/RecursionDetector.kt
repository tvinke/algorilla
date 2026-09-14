package com.github.tvinke.algorilla.util

import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl

// Receiver/arity-aware recursion detection. Split out from IRNodeExtensions.kt
// on purpose — this is the one place that decides whether a call is a genuine self-reference,
// replacing what used to be seven separate name-only `it.name == fn.name` checks scattered
// across UnmemoizedRecursionRule, NestedLookupRule and HiddenNestedLoopRule, all of which
// mistook `super.foo()` and same-arity sibling overloads for recursion.

/**
 * Receivers that, combined with a matching name, prove a [FunctionCall] refers back to the
 * *same* method: no receiver at all (implicit `this`) or an explicit `this`. Deliberately
 * excludes `super` — `super.foo()` dispatches to the superclass's implementation, not this
 * one, so it is a delegation, not a repeat of the same call (the Broadleaf
 * `super.getSectionKey()` false positive).
 */
private val SELF_CALL_RECEIVERS: Set<String?> = setOf(null, "this")

/**
 * Returns true if this [FunctionCall] is a genuine call to [target]'s own declaration:
 * matching name, a receiver that provably refers to this object (not `super`, not some
 * other variable or field), and matching arity. When a [symbolTable] is supplied, also
 * guards against sibling overloads: if another method with the same name and the same
 * parameter count exists in [target]'s declaring class, which one the call actually
 * targets is ambiguous without full argument-type resolution, and this conservatively
 * returns false rather than guessing (the Fineract `modifyLoanApprovedAmount`
 * sibling-overload false positive).
 */
public fun FunctionCall.isSelfCallOf(
    target: FunctionDecl,
    symbolTable: SymbolTable? = null,
): Boolean {
    if (name != target.name) return false
    if (qualifiedTarget !in SELF_CALL_RECEIVERS) return false
    if (arguments.size != target.parameters.size) return false
    if (symbolTable != null) {
        // Same-class overloads are already indexed by declaring class — narrower and
        // cheaper than filtering the whole same-simple-name bucket by hand. Falls back to
        // the simple-name index for top-level functions, which lookupByClassAndName can't
        // key on (it only registers declarations that have a declaringClass).
        val sameArityOverloads =
            if (target.declaringClass != null) {
                symbolTable
                    .lookupByClassAndName("${target.declaringClass}.${target.name}")
                    .filter { it.parameters.size == arguments.size }
            } else {
                symbolTable
                    .lookupBySimpleName(name)
                    .filter { it.declaringClass == null && it.parameters.size == arguments.size }
            }
        if (sameArityOverloads.size > 1) return false
    }
    return true
}

/**
 * Returns true if the function calls itself directly (recursive method).
 * Recursive methods (tree walkers, visitors, DFS) contain loops that iterate
 * child nodes — total work is O(tree_size), not O(n²).
 *
 * Pass a [symbolTable] when available so a same-name, same-arity sibling overload in the
 * same class does not get mistaken for a self-call — see [isSelfCallOf].
 */
public fun FunctionDecl.isRecursive(symbolTable: SymbolTable? = null): Boolean =
    findDescendants<FunctionCall>().any { it.isSelfCallOf(this, symbolTable) }
