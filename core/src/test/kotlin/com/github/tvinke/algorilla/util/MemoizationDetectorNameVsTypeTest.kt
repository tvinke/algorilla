package com.github.tvinke.algorilla.util

import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.SourceLocation
import com.github.tvinke.algorilla.model.VariableDecl
import com.github.tvinke.algorilla.semantics.LanguageSemanticsRegistry
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

/**
 * Canary properties for the testharnas campaign. hasVisitedTracking's Set
 * add()/contains() check hardcodes the literal method name "contains" regardless of
 * [Language] — but this rule (UnmemoizedRecursionRule) runs on every language, and
 * JavaScript/TypeScript's real `Set` doesn't have a `contains()` method at all, it has
 * `has()` (already classified in javascript.yml's methods section with
 * `kind: CONTAINS`, same as Java/Kotlin/Groovy's `contains()`). A TypeScript function
 * memoizing recursion with `const visited: Set<string> = new Set(); ...
 * visited.has(x); ... visited.add(x)` was never recognized as visited-tracking, so
 * UnmemoizedRecursionRule would false-positive on code that already guards against
 * unbounded recursion.
 */
internal class MemoizationDetectorNameVsTypeTest {
    private val loc = SourceLocation("Fixture.ts", 1, 1)
    private val registry = LanguageSemanticsRegistry.loadDefaults()

    @Test
    fun `a TypeScript Set using has() for the contains check is still recognized as visited-tracking`() {
        val fn = visitedSetFunction(containsMethodName = "has")
        MemoizationDetector.hasVisitedTracking(fn, Language.TYPESCRIPT, registry) shouldBe true
    }

    @Test
    fun `a Java-style Set using contains() is still recognized as visited-tracking`() {
        val fn = visitedSetFunction(containsMethodName = "contains")
        MemoizationDetector.hasVisitedTracking(fn, Language.JAVA, registry) shouldBe true
    }

    private fun visitedSetFunction(containsMethodName: String): FunctionDecl {
        val visitedVar = VariableDecl("encounteredNodes", "Set", location = loc, children = emptyList())
        val addCall = FunctionCall("add", "encounteredNodes", emptyList(), loc, emptyList())
        val containsCall = FunctionCall(containsMethodName, "encounteredNodes", emptyList(), loc, emptyList())
        return FunctionDecl(
            name = "walk",
            qualifiedName = "Fixture.walk",
            parameters = emptyList(),
            declaringClass = "Fixture",
            location = loc,
            children = listOf(visitedVar, addCall, containsCall),
        )
    }
}
