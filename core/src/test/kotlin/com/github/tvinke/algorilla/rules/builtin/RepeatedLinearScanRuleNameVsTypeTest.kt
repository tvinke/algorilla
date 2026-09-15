package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.SourceLocation
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Finding
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import org.junit.jupiter.api.Test

/**
 * Canary property for the testharnas campaign. isCollectionVariable's
 * first-letter-case check ("starts lowercase" = variable, "starts uppercase" = class
 * reference like Collectors/Stream) is a case-convention heuristic, unlike the other
 * checks fixed this campaign, but a well-founded one — Java/Kotlin/Groovy naming
 * convention makes this a reliable signal in idiomatic code, and no natural counter-example
 * (a real collection variable capitalized like a class, or a class reference lowercased
 * like a variable) was found worth pinning as a bug. Property tests below lock in the
 * intended behavior instead.
 */
internal class RepeatedLinearScanRuleNameVsTypeTest {
    private val loc = SourceLocation("Fixture.java", 1, 1)
    private val rule = RepeatedLinearScanRule()

    @Test
    fun `repeated full-scan calls on a lowercase variable are flagged`() {
        findingsFor("orders") shouldHaveSize 1
    }

    @Test
    fun `repeated full-scan calls on an uppercase class reference are excluded`() {
        findingsFor("Collectors").shouldBeEmpty()
    }

    private fun findingsFor(target: String): List<Finding> {
        val call1 = FunctionCall("groupBy", target, emptyList(), SourceLocation("Fixture.java", 1, 1), emptyList())
        val call2 = FunctionCall("distinct", target, emptyList(), SourceLocation("Fixture.java", 20, 1), emptyList())
        val fn =
            FunctionDecl(
                name = "process",
                qualifiedName = "Fixture.process",
                parameters = emptyList(),
                declaringClass = "Fixture",
                location = loc,
                children = listOf(call1, call2),
            )
        val fileRoot = FileRoot(filePath = "Fixture.java", language = Language.JAVA, location = loc, children = listOf(fn))
        val context =
            AnalysisContext(
                irTrees = mapOf("Fixture.java" to fileRoot),
                symbolTable = SymbolTable(),
                callGraph = CallGraph(),
                config = AnalysisConfig(),
            )
        return rule.evaluate(context)
    }
}
