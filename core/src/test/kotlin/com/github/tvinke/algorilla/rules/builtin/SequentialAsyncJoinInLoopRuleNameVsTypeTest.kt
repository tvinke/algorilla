package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LoopKind
import com.github.tvinke.algorilla.model.LoopNode
import com.github.tvinke.algorilla.model.Parameter
import com.github.tvinke.algorilla.model.SourceLocation
import com.github.tvinke.algorilla.model.VariableDecl
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.signatureKey
import com.github.tvinke.algorilla.semantics.LanguageSemanticsRegistry
import com.github.tvinke.algorilla.semantics.TypeEnvironment
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import org.junit.jupiter.api.Test

/**
 * Canary property for looksLikeFutureCall.
 * The target was lowercased before a bare contains() against future-indicators (future,
 * promise, async, completable, deferred, task), no boundary either side. "task" is a common
 * enough word that "subtask" (a generic subtask concept, not necessarily async) satisfied it
 * too, and the lowercasing destroyed the camelCase signal a boundary check needs.
 */
internal class SequentialAsyncJoinInLoopRuleNameVsTypeTest {
    private val loc = SourceLocation("Fixture.java", 1, 1)
    private val rule = SequentialAsyncJoinInLoopRule()

    @Test
    fun `a blocking join on a genuine future-named target is still flagged`() {
        findingsFor("myTask") shouldHaveSize 1
        findingsFor("future") shouldHaveSize 1
    }

    @Test
    fun `a blocking join on a target merely containing task without a boundary is not flagged`() {
        findingsFor("subtask").shouldBeEmpty()
    }

    /**
     * "userTask" boundary-matches the "task" indicator by name, but its declared type here
     * is an unrelated domain class - once a TypeEnvironment is available, the declared type
     * wins and this is no longer treated as a Future.
     */
    @Test
    fun `a variable named like a future indicator but declared as something else is not flagged`() {
        findingsForWithDeclaredType("userTask", "WorkItem").shouldBeEmpty()
    }

    @Test
    fun `a variable not named like a future indicator but declared as CompletableFuture is still flagged`() {
        findingsForWithDeclaredType("handle", "CompletableFuture") shouldHaveSize 1
    }

    /**
     * "myTask" boundary-matches the "task" indicator by name - the name heuristic alone
     * would correctly flag it. But its only type evidence here comes from an initializer's
     * method-name suffix ("helper.getResultList()" ends in "List") - the lowest-trust
     * NAME_HEURISTIC source, same one isCollection/isO1 already refuse to act on. A raw
     * typeOf().simpleName read that irrelevant "List" guess as a real declared type, found
     * it doesn't match futureIndicators, and returned false without ever reaching the name
     * check - silently swallowing a real sequential-blocking-join finding.
     */
    @Test
    fun `a future-indicator-named variable with only a name-heuristic-inferred type is still flagged`() {
        val varName = "myTask"
        val listyInit = FunctionCall("getResultList", "helper", emptyList(), loc, emptyList())
        val varDecl = VariableDecl(varName, null, initializer = listyInit, location = loc, children = listOf(listyInit))
        val call = FunctionCall("join", varName, emptyList(), loc, emptyList())
        val loop = LoopNode(kind = LoopKind.FOR_EACH, iteratedVariable = "items", location = loc, children = listOf(call))
        val fn =
            FunctionDecl(
                name = "process",
                qualifiedName = "Fixture.process",
                parameters = emptyList(),
                declaringClass = "Fixture",
                location = loc,
                children = listOf(varDecl, loop),
            )
        val registry = LanguageSemanticsRegistry.DEFAULT
        val typeEnv = TypeEnvironment.build(fn, emptyMap(), Language.JAVA, registry)
        val fileRoot = FileRoot(filePath = "Fixture.java", language = Language.JAVA, location = loc, children = listOf(fn))
        val context =
            AnalysisContext(
                irTrees = mapOf("Fixture.java" to fileRoot),
                symbolTable = SymbolTable(),
                callGraph = CallGraph(),
                config = AnalysisConfig(),
                registry = registry,
                typeEnvironments = mapOf(signatureKey(fn) to typeEnv),
            )
        rule.evaluate(context) shouldHaveSize 1
    }

    private fun findingsForWithDeclaredType(
        target: String,
        declaredType: String,
    ): List<Finding> {
        val call = FunctionCall("join", target, emptyList(), loc, emptyList())
        val loop = LoopNode(kind = LoopKind.FOR_EACH, iteratedVariable = "items", location = loc, children = listOf(call))
        val fn =
            FunctionDecl(
                name = "process",
                qualifiedName = "Fixture.process",
                parameters = listOf(Parameter(target, declaredType)),
                declaringClass = "Fixture",
                location = loc,
                children = listOf(loop),
            )
        val registry = LanguageSemanticsRegistry.DEFAULT
        val typeEnv = TypeEnvironment.build(fn, emptyMap(), Language.JAVA, registry)
        val fileRoot = FileRoot(filePath = "Fixture.java", language = Language.JAVA, location = loc, children = listOf(fn))
        val context =
            AnalysisContext(
                irTrees = mapOf("Fixture.java" to fileRoot),
                symbolTable = SymbolTable(),
                callGraph = CallGraph(),
                config = AnalysisConfig(),
                registry = registry,
                typeEnvironments = mapOf(signatureKey(fn) to typeEnv),
            )
        return rule.evaluate(context)
    }

    private fun findingsFor(target: String): List<Finding> {
        val call = FunctionCall("join", target, emptyList(), loc, emptyList())
        val loop = LoopNode(kind = LoopKind.FOR_EACH, iteratedVariable = "items", location = loc, children = listOf(call))
        val fileRoot = FileRoot(filePath = "Fixture.java", language = Language.JAVA, location = loc, children = listOf(loop))
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
