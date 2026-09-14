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

/**
 * Returns true if [word] occurs anywhere in [text] as a whole identifier segment — a real
 * boundary (start/end of string, a camelCase transition, or a non-letter separator) on
 * *both* sides of the match, not just one. Plain `contains` matches "repo" inside
 * "reportGenerator" (the "repo" happens to sit at the very start of "report", so a
 * leading-only boundary check would miss it) and "store" inside "storefront" — neither
 * has anything to do with a repository. Checks every occurrence in case an earlier one
 * fails the boundary test but a later one doesn't.
 */
public fun containsAtWordBoundary(
    text: String,
    word: String,
    ignoreCase: Boolean = true,
): Boolean {
    if (word.isEmpty()) return false
    var fromIndex = 0
    while (true) {
        val idx = text.indexOf(word, fromIndex, ignoreCase = ignoreCase)
        if (idx < 0) return false
        val leadingOk = idx == 0 || text[idx].isUpperCase() || !text[idx - 1].isLetter()
        val trailingIdx = idx + word.length
        val trailingOk = trailingIdx >= text.length || !text[trailingIdx].isLetterOrDigit() || text[trailingIdx].isUpperCase()
        if (leadingOk && trailingOk) return true
        fromIndex = idx + 1
    }
}
