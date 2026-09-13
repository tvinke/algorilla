package com.github.tvinke.algorilla.util

/*
 * Word-boundary-aware prefix/suffix matching, shared by the rules that classify a
 * method or variable name against a configured prefix/suffix list. Plain `startsWith`/
 * `endsWith` over-match: "get" matches inside "getaway", "writer" matches inside
 * "screenwriter", "In" matches the tail of "findByOrigin". These require a real camelCase
 * transition (or a non-letter separator) at the boundary, not just an arbitrary run of
 * letters that happens to line up — the same "name looks right, isn't" failure this
 * campaign (cc #73) keeps finding, applied consistently wherever a prefix/suffix list is
 * matched against free-form identifier text.
 */

/**
 * Returns true if [text] starts with [prefix] and either that's the whole string, or the
 * next character is uppercase/a digit (`getUser` for `get`) or not a letter at all (a
 * separator like `_`). Rejects a plain lowercase continuation (`getaway` for `get`,
 * `findAllocation` for `findAll`).
 */
public fun startsWithAtWordBoundary(
    text: String,
    prefix: String,
    ignoreCase: Boolean = true,
): Boolean {
    if (!text.startsWith(prefix, ignoreCase = ignoreCase)) return false
    if (text.length == prefix.length) return true
    val boundaryChar = text[prefix.length]
    return boundaryChar.isUpperCase() || boundaryChar.isDigit() || !boundaryChar.isLetter()
}

/**
 * Returns true if [text] ends with [suffix] and either that's the whole string, or the
 * matched suffix itself starts with an uppercase letter (`hibernateSession` for `session`)
 * or the character right before it isn't a letter (a separator). Rejects a plain lowercase
 * run into the match (`screenwriter` for `writer`, `findByOrigin` for `In`).
 */
public fun endsWithAtWordBoundary(
    text: String,
    suffix: String,
    ignoreCase: Boolean = true,
): Boolean {
    if (!text.endsWith(suffix, ignoreCase = ignoreCase)) return false
    val matchStart = text.length - suffix.length
    if (matchStart <= 0) return true
    if (text[matchStart].isUpperCase()) return true
    return !text[matchStart - 1].isLetter()
}
