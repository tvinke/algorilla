package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.GenericNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.SourceLocation
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.semantics.LanguageSemanticsRegistry
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import org.junit.jupiter.api.Test

/**
 * Canary property for the testharnas campaign (cc #73). Same isGetterPattern bug as
 * ChainedGettersRule and RedundantExpensiveCallRule: "loader()"/"resolver()" start with
 * getter-prefixes "load"/"resolve" with no camelCase boundary and get misread as getter
 * calls, producing a false uncached-getter finding when called twice with the same
 * argument.
 */
internal class UncachedGetterRuleNameVsTypeTest {
    private val loc = SourceLocation("Fixture.java", 1, 1)
    private val rule = UncachedGetterRule()
    private val registry = LanguageSemanticsRegistry.loadDefaults()

    @Test
    fun `loader and resolver calls are not misread as getters just because of the prefix`() {
        evaluateTwoIdenticalCalls("loader").shouldBeEmpty()
        evaluateTwoIdenticalCalls("resolver").shouldBeEmpty()
    }

    @Test
    fun `a genuine repeated getter call is still flagged`() {
        evaluateTwoIdenticalCalls("getOrder") shouldHaveSize 1
    }

    private fun evaluateTwoIdenticalCalls(callName: String): List<Finding> {
        val arg = GenericNode("id", loc, emptyList())
        val call1 = FunctionCall(callName, "service", listOf(arg), loc, emptyList())
        val call2 = FunctionCall(callName, "service", listOf(arg), loc, emptyList())
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
                registry = registry,
            )
        return rule.evaluate(context)
    }
}
