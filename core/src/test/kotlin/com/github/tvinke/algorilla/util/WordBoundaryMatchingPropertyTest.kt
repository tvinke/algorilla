package com.github.tvinke.algorilla.util

import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.constant
import io.kotest.property.arbitrary.flatMap
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.of
import io.kotest.property.checkAll
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

/**
 * Genuinely generative property tests for [startsWithAtWordBoundary] and
 * [endsWithAtWordBoundary] themselves — the shared primitive behind every "missing word
 * boundary" fix in the testharnas campaign (cc #73), used directly or indirectly by 5+
 * rule files, but with no test file of its own until now. Every property test written
 * earlier in this campaign was `Arb.of(handPickedList)` — a fine regression net for bugs
 * already found, but no evidence against undiscovered ones. These generate random
 * word/continuation combinations on every run and check the result against an
 * independently computed expectation (a regex over the boundary character, not a call
 * into the function under test), rather than a fixed example list.
 */
internal class WordBoundaryMatchingPropertyTest {
    private val lowerLetters = ('a'..'z').toList()
    private val upperLetters = ('A'..'Z').toList()
    private val digits = ('0'..'9').toList()
    private val separators = listOf('_', '-', '.', '@')

    /** A boundary character is upper/digit/non-letter — the independent regex oracle. */
    private val boundaryCharRegex = Regex("^[A-Z0-9]|^[^A-Za-z]")

    private fun wordArb(lengthRange: IntRange = 1..6): Arb<String> =
        Arb
            .list(Arb.int(0 until lowerLetters.size), lengthRange)
            .map { indices -> indices.joinToString("") { lowerLetters[it].toString() } }

    private fun charFrom(pool: List<Char>): Arb<Char> = Arb.int(0 until pool.size).map { pool[it] }

    /**
     * Builds a random "continuation" string after a prefix/suffix match, tagged with
     * whether a human would call the transition a real word boundary — established by
     * construction (which category was picked), not by asking the function under test.
     */
    private val continuationArb: Arb<String> =
        Arb.of(0, 1, 2, 3, 4).flatMap { kind ->
            when (kind) {
                0 -> Arb.constant("") // exact match, nothing follows
                1 -> charFrom(upperLetters).flatMap { u -> wordArb(0..5).map { "$u$it" } } // camelCase
                2 -> charFrom(digits).flatMap { d -> wordArb(0..5).map { "$d$it" } } // digit
                3 -> charFrom(separators).flatMap { s -> wordArb(0..5).map { "$s$it" } } // separator
                else -> charFrom(lowerLetters).flatMap { l -> wordArb(0..5).map { "$l$it" } } // no boundary
            }
        }

    @Test
    fun `startsWithAtWordBoundary agrees with an independent regex oracle over random word-continuation pairs`() {
        runBlocking {
            checkAll(wordArb(), continuationArb) { prefix, continuation ->
                val text = prefix + continuation
                val expected = continuation.isEmpty() || boundaryCharRegex.containsMatchIn(continuation.take(1))
                startsWithAtWordBoundary(text, prefix) shouldBe expected
            }
        }
    }

    @Test
    fun `startsWithAtWordBoundary is case-insensitive on the prefix itself, boundary aside`() {
        runBlocking {
            checkAll(wordArb(), continuationArb) { prefix, continuation ->
                val text = prefix.uppercase() + continuation
                val expected = continuation.isEmpty() || boundaryCharRegex.containsMatchIn(continuation.take(1))
                startsWithAtWordBoundary(text, prefix) shouldBe expected
            }
        }
    }

    /**
     * Same shape for the suffix side, but the boundary character sits right before the
     * match (or the match's own leading character is what's uppercase) rather than right
     * after it — built independently for this direction too.
     */
    private val precedingArb: Arb<Pair<String, Boolean>> =
        Arb.of(0, 1, 2).flatMap { kind ->
            when (kind) {
                0 -> Arb.constant("" to true) // empty - trivially a boundary
                1 -> charFrom(separators).flatMap { s -> wordArb(0..5).map { "$it$s" to true } } // ends in separator
                else -> wordArb(1..6).map { it to false } // ends in a plain lowercase letter - no boundary from this side
            }
        }

    @Test
    fun `endsWithAtWordBoundary agrees with an independent oracle over random preceding-plus-suffix combinations`() {
        runBlocking {
            checkAll(precedingArb, wordArb(1..6), Arb.of(true, false)) { (preceding, precedingIsBoundary), suffix, capitalizeMatch ->
                val matchOccurrence = if (capitalizeMatch) suffix.replaceFirstChar { it.uppercaseChar() } else suffix
                val text = preceding + matchOccurrence
                val expected = preceding.isEmpty() || capitalizeMatch || precedingIsBoundary
                endsWithAtWordBoundary(text, suffix) shouldBe expected
            }
        }
    }
}
