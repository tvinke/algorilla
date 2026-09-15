package com.github.tvinke.algorilla.util

import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.VariableDecl

/**
 * Finds the [VariableDecl] named [name] in [scope] and returns its initializer as a
 * [FunctionCall], or null when [name] isn't declared in [scope] or its initializer isn't
 * a function call.
 *
 * This is the general form of "resolve a variable name back to what it was actually
 * assigned from" — originally hand-rolled inline in [com.github.tvinke.algorilla.graph.LoopBoundAnnotator]
 * for factory-collection detection. Callers that only have a bare variable name (because
 * the real value was copied into a local before use) reach for this instead of guessing
 * from the name itself — a local named `values` is not necessarily a map's values just
 * because it looks like one.
 *
 * Deliberately resolves exactly one hop: a chain of copies (`val a = b; val b = c.foo()`)
 * is out of scope, matching the depth [com.github.tvinke.algorilla.graph.LoopBoundAnnotator]'s
 * original inline version already resolved. Callers that need [name]'s initializer as text
 * (not just as a [FunctionCall]) can still fall back to [name] itself when this returns null.
 *
 * Known limitation, inherited unchanged from that original inline version: [scope] is a flat
 * list with no notion of declaration order or block position, so two unrelated declarations
 * sharing [name] within the same [scope] (e.g. the same generic local name reused in two
 * separate loops in one function) resolve to whichever one [scope] happens to list first, not
 * necessarily the one actually in scope at the caller's use site. Callers narrow [scope] as
 * tightly as they reasonably can (per enclosing function, not per file) to shrink this window,
 * but a proper fix needs position-aware filtering against [VariableDecl.location] - out of
 * scope for the callers this currently serves.
 */
public fun resolveInitializer(
    name: String,
    scope: List<VariableDecl>,
): FunctionCall? = scope.firstOrNull { it.name == name }?.initializer as? FunctionCall
