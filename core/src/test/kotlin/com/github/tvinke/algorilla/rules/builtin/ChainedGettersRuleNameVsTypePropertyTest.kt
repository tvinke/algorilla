package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.GenericNode
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LookupCall
import com.github.tvinke.algorilla.model.LookupKind
import com.github.tvinke.algorilla.model.SourceLocation
import com.github.tvinke.algorilla.model.VariableDecl
import com.github.tvinke.algorilla.rules.AnalysisContext
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import org.junit.jupiter.api.Test

/**
 * Canary property for the testharnas campaign (cc #73). isGetterPattern's
 * `getterPrefixes.any { call.name.startsWith(it, ignoreCase = true) }` has the same
 * missing word boundary already fixed in RedundantExpensiveCallRule's isGetterPattern:
 * "loader()"/"resolver()" (a method returning a Loader/Resolver utility, not a chainable
 * getter itself) start with "load"/"resolve" with no real boundary and get misread as
 * getter calls, feeding a false chained-getter-cascade finding.
 */
internal class ChainedGettersRuleNameVsTypePropertyTest {
    private val loc = SourceLocation("Fixture.java", 1, 1)
    private val rule = ChainedGettersRule()

    @Test
    fun `loader and resolver are not misread as getters just because of the prefix`() {
        val orderDecl = varDecl("order", call("loader"))
        val chainCall = call("resolver", ref("order"))
        val fn = functionDecl("process", orderDecl, chainCall)

        rule.evaluate(context(fn, resolvableLookupFn("resolver"))).shouldBeEmpty()
    }

    @Test
    fun `a genuine getter chain is still recognized`() {
        val orderDecl = varDecl("order", call("getOrder"))
        val chainCall = call("getCustomer", ref("order"))
        val fn = functionDecl("process", orderDecl, chainCall)

        rule.evaluate(context(fn, resolvableLookupFn("getCustomer"))) shouldHaveSize 1
    }

    /** A resolvable [FunctionDecl] with a [LookupCall] body, so the chain-length check passes. */
    private fun resolvableLookupFn(name: String) =
        FunctionDecl(
            name = name,
            qualifiedName = "Repo.$name",
            parameters = emptyList(),
            declaringClass = "Repo",
            location = loc,
            children =
                listOf(
                    LookupCall(kind = LookupKind.FIND, targetVariable = "items", isO1 = false, location = loc, children = emptyList()),
                ),
        )

    private fun context(
        fn: FunctionDecl,
        resolvable: FunctionDecl,
    ): AnalysisContext {
        val symbolTable = SymbolTable().also { it.register(resolvable) }
        val fileRoot = FileRoot(filePath = "Fixture.java", language = Language.JAVA, location = loc, children = listOf(fn))
        return AnalysisContext(
            irTrees = mapOf("Fixture.java" to fileRoot),
            symbolTable = symbolTable,
            callGraph = CallGraph(),
            config = AnalysisConfig(),
        )
    }

    private fun functionDecl(
        name: String,
        vararg body: IRNode,
    ) = FunctionDecl(
        name = name,
        qualifiedName = "Fixture.$name",
        parameters = emptyList(),
        declaringClass = "Fixture",
        location = loc,
        children = body.toList(),
    )

    private fun varDecl(
        name: String,
        initializer: FunctionCall,
    ) = VariableDecl(name = name, typeName = null, initializer = initializer, location = loc, children = listOf(initializer))

    private fun call(
        name: String,
        vararg arguments: IRNode,
    ) = FunctionCall(name = name, qualifiedTarget = null, arguments = arguments.toList(), location = loc, children = emptyList())

    private fun ref(name: String) = GenericNode(nodeType = name, location = loc, children = emptyList())
}
