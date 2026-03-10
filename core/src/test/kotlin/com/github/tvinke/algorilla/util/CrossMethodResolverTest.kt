package com.github.tvinke.algorilla.util

import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.SourceLocation
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class CrossMethodResolverTest {
    private val loc = SourceLocation("test.java", 1, 1)

    @Test
    fun `should resolve call via variable type when direct lookup fails`() {
        val table = SymbolTable()
        val fetchUsers = makeDecl("fetchUsers", "UserService.fetchUsers", "UserService")
        table.register(fetchUsers)
        table.registerType("service", "UserService")

        val call = makeCall("fetchUsers", "service")

        CrossMethodResolver.resolve(call, table) shouldBe fetchUsers
    }

    @Test
    fun `should return null when variable type is unknown`() {
        val table = SymbolTable()
        val call = makeCall("fetchUsers", "service")

        CrossMethodResolver.resolve(call, table).shouldBeNull()
    }

    @Test
    fun `should prefer direct match over type lookup`() {
        val table = SymbolTable()
        val directMatch = makeDecl("process", "service.process", "service")
        val typeMatch = makeDecl("process", "OrderService.process", "OrderService")
        table.register(directMatch)
        table.register(typeMatch)
        table.registerType("service", "OrderService")

        val call = makeCall("process", "service")

        CrossMethodResolver.resolve(call, table) shouldBe directMatch
    }

    private fun makeDecl(
        name: String,
        qualifiedName: String,
        declaringClass: String,
    ) = FunctionDecl(
        name = name,
        qualifiedName = qualifiedName,
        parameters = emptyList(),
        declaringClass = declaringClass,
        location = loc,
        children = emptyList(),
    )

    private fun makeCall(
        name: String,
        target: String,
    ) = FunctionCall(
        name = name,
        qualifiedTarget = target,
        arguments = emptyList(),
        location = loc,
        children = emptyList(),
    )
}
