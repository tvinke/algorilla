package com.github.tvinke.algorilla.engine

import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LookupCall
import com.github.tvinke.algorilla.model.LookupKind
import com.github.tvinke.algorilla.model.SourceLocation
import com.github.tvinke.algorilla.model.VariableDecl
import com.github.tvinke.algorilla.semantics.LanguageSemanticsRegistry
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

/**
 * Regression coverage for #140: [markScalarLookupsInFunction] read
 * [com.github.tvinke.algorilla.semantics.TypeEnvironment.isCollection] returning `false` as
 * proof the target is scalar, but `false` there means "can't confirm it's a collection" - it
 * doesn't distinguish a genuinely non-collection declared type from a
 * [com.github.tvinke.algorilla.model.TypeSource.NAME_HEURISTIC] guess that isCollection
 * deliberately distrusts (see its kdoc). Same trust-source bug shape as #139, expressed as a
 * lossy Boolean instead of a discarded enum: a lookup on a variable whose only type evidence is
 * a method-name suffix (`orderList = getOrderList()`, guessed "List") got wrongly marked
 * `isScalar = true`, suppressing genuine linear-lookup findings downstream (rules filter out
 * `isScalar` lookups as definitely-not-worth-flagging).
 */
internal class ScalarLookupMarkerNameVsTypeTest {
    private val loc = SourceLocation("Fixture.java", 1, 1)

    @Test
    fun `does not mark a NAME_HEURISTIC-typed lookup target as scalar merely because isCollection can't confirm it`() {
        val init =
            FunctionCall(name = "getOrderList", qualifiedTarget = null, arguments = emptyList(), location = loc, children = emptyList())
        val varDecl = VariableDecl(name = "orderList", typeName = null, initializer = init, location = loc, children = listOf(init))
        val lookup =
            LookupCall(kind = LookupKind.CONTAINS, targetVariable = "orderList", isO1 = false, location = loc, children = emptyList())
        val fn =
            FunctionDecl(
                name = "process",
                qualifiedName = "Fixture.process",
                parameters = emptyList(),
                declaringClass = "Fixture",
                location = loc,
                children = listOf(varDecl, lookup),
            )
        val fileRoot = FileRoot(filePath = "Fixture.java", language = Language.JAVA, location = loc, children = listOf(fn))

        val result = markScalarLookups(mapOf("Fixture.java" to fileRoot), LanguageSemanticsRegistry.DEFAULT)

        val transformedLookup =
            (
                result.irTrees
                    .getValue("Fixture.java")
                    .children
                    .single() as FunctionDecl
            ).children.filterIsInstance<LookupCall>().single()
        // Before the fix: isCollection("orderList") returned false purely because its only type
        // evidence is a NAME_HEURISTIC guess ("List", from the "getOrderList" suffix) - not
        // because the variable is confirmed scalar - and that false alone was enough to set
        // isScalar = true, suppressing a real linear-lookup finding on a genuine List.
        transformedLookup.isScalar shouldBe false
        transformedLookup.isO1 shouldBe false
    }

    @Test
    fun `still marks a lookup scalar when the declared type genuinely is not a collection`() {
        val varDecl = VariableDecl(name = "name", typeName = "String", initializer = null, location = loc, children = emptyList())
        val lookup =
            LookupCall(kind = LookupKind.CONTAINS, targetVariable = "name", isO1 = false, location = loc, children = emptyList())
        val fn =
            FunctionDecl(
                name = "process",
                qualifiedName = "Fixture.process",
                parameters = emptyList(),
                declaringClass = "Fixture",
                location = loc,
                children = listOf(varDecl, lookup),
            )
        val fileRoot = FileRoot(filePath = "Fixture.java", language = Language.JAVA, location = loc, children = listOf(fn))

        val result = markScalarLookups(mapOf("Fixture.java" to fileRoot), LanguageSemanticsRegistry.DEFAULT)

        val transformedLookup =
            (
                result.irTrees
                    .getValue("Fixture.java")
                    .children
                    .single() as FunctionDecl
            ).children.filterIsInstance<LookupCall>().single()
        transformedLookup.isScalar shouldBe true
    }
}
