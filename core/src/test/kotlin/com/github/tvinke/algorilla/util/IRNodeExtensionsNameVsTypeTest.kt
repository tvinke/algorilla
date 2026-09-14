package com.github.tvinke.algorilla.util

import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LookupCall
import com.github.tvinke.algorilla.model.LookupKind
import com.github.tvinke.algorilla.model.Parameter
import com.github.tvinke.algorilla.model.SourceLocation
import com.github.tvinke.algorilla.semantics.LanguageSemanticsRegistry
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

/**
 * Canary properties for the testharnas campaign (cc #73). isCollectionLookup ran
 * hasO1TargetName — a bare name heuristic ("cache"/"table"/"queue"/... suffixes) —
 * *before* ever consulting the resolved type (TypeEnvironment or the declared parameter/
 * variable type), unlike every other declared-type-vs-heuristic fix in this campaign,
 * where the heuristic was at least a fallback. Here it ran unconditionally first: a
 * genuinely declared `List<User> userCache` parameter got read as "false" (O(1)/skip)
 * purely from its name, before the real declared type — List, not O(1) at all — ever got
 * a say. Used by both NestedLookupRule and RepeatedLinearScanRule, so this silently
 * suppressed real O(n) lookup findings on any collection whose name happened to look like
 * a Map/cache.
 */
internal class IRNodeExtensionsNameVsTypeTest {
    private val loc = SourceLocation("Fixture.java", 1, 1)
    private val registry = LanguageSemanticsRegistry.loadDefaults()

    private val nameSuffixCollisions =
        listOf(
            "userCache" to "List",
            "lookupTable" to "ArrayList",
            "requestQueue" to "List",
        )

    @Test
    fun `a declared List whose name coincidentally suggests a Map is still a real collection lookup`() {
        nameSuffixCollisions.forEach { (varName, declaredType) ->
            lookupFor(varName).isCollectionLookup(fnWith(varName, declaredType), null, Language.JAVA, registry) shouldBe true
        }
    }

    @Test
    fun `a real Map is still excluded regardless of its variable name`() {
        val fn = fnWith("orders", "HashMap")
        lookupFor("orders").isCollectionLookup(fn, null, Language.JAVA, registry) shouldBe false
    }

    @Test
    fun `an untyped variable ending in a Map suffix still falls back to the name heuristic`() {
        // No declared type at all (fn is null, matching NestedLookupRule's second call site
        // that passes fn=null/typeEnv=null) - the heuristic fallback must still apply.
        val lookup = lookupFor("configCache")
        lookup.isCollectionLookup(null, null, Language.JAVA, registry) shouldBe false
    }

    @Test
    fun `hasO1TargetName's own suffix match also needs a word boundary, even with no type info at all`() {
        // "vegetable" ends in "table" (a non-list-target suffix) but is not "a table" in
        // any collection sense - same fix as RegexRecompilationInLoopRule's isMapTarget.
        val lookup = lookupFor("vegetable")
        lookup.isCollectionLookup(null, null, Language.JAVA, registry) shouldBe true
    }

    private fun lookupFor(varName: String) =
        LookupCall(kind = LookupKind.FIND, targetVariable = varName, isO1 = false, location = loc, children = emptyList())

    private fun fnWith(
        varName: String,
        declaredType: String,
    ) = FunctionDecl(
        name = "process",
        qualifiedName = "Fixture.process",
        parameters = listOf(Parameter(varName, declaredType)),
        declaringClass = "Fixture",
        location = loc,
        children = emptyList(),
    )
}
