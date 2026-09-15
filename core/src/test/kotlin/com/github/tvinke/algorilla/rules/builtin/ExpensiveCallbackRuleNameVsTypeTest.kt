package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.GenericNode
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
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

/**
 * Canary properties for the testharnas campaign. isCompileCall here is the exact
 * same code as RepeatedRegexInLoopRule's — `qualifiedTarget?.contains("Pattern")` /
 * `contains("Regex")` matches anywhere in the receiver name, so a class like
 * DateTimePatternValidator.compile() (nothing to do with java.util.regex.Pattern) gets
 * misread as a regex compile inside a callback, both producing a false "regex compiled per
 * callback invocation" finding here AND (line 134) wrongly suppressing whatever the
 * generic-expensive-callback check would otherwise have said about that same call.
 */
internal class ExpensiveCallbackRuleNameVsTypeTest {
    private val loc = SourceLocation("Fixture.java", 1, 1)
    private val rule = ExpensiveCallbackRule()

    private val unrelatedClassesContainingRegexWords =
        listOf(
            "DateTimePatternValidator",
            "NamingRegexResolver",
        )

    @Test
    fun `a class whose name merely contains 'Pattern' or 'Regex' does not trigger a false regex-compile finding`() {
        unrelatedClassesContainingRegexWords.forEach { target ->
            findingsFor(target).none { it.message.contains("Regex compilation", ignoreCase = true) } shouldBe true
        }
    }

    @Test
    fun `a real Pattern-compile call inside a callback is still flagged`() {
        val findings = findingsFor("Pattern")
        findings shouldHaveSize 1
    }

    /**
     * "userPattern" ends with "Pattern" at a real word boundary, so the name heuristic alone
     * would still misread this as a regex compile - but its declared type is a domain class
     * that merely happens to have its own compile() method, nothing to do with
     * java.util.regex.Pattern. With a TypeEnvironment available, the declared type wins.
     */
    @Test
    fun `a variable named like a regex type but declared as something else is not flagged`() {
        findingsForWithDeclaredType("userPattern", "PatternValidator").shouldBeEmpty()
    }

    @Test
    fun `a variable not named like a regex type but declared as Pattern is still flagged`() {
        findingsForWithDeclaredType("compiler", "Pattern") shouldHaveSize 1
    }

    /**
     * Same declared-type-wins fix, applied to this rule's date-parse detection (isDateParseCall,
     * shared with ExpensiveSortComparatorRule) rather than its regex-compile detection above -
     * "myDateTimeFormatterHelper" boundary-matches "DateTimeFormatter" by name, but its declared
     * type here is an unrelated formatter class.
     */
    @Test
    fun `a variable named like a date-parse target but declared as something else is not flagged as date parsing`() {
        val call = FunctionCall("parse", "myDateTimeFormatterHelper", emptyList(), loc, emptyList())
        val callback = LoopNode(kind = LoopKind.HIGHER_ORDER, iteratedVariable = "items", location = loc, children = listOf(call))
        findingsForCallbackWithDeclaredType(callback, "myDateTimeFormatterHelper", "CustomFormatter").shouldBeEmpty()
    }

    /**
     * "myRegex" ends with "Regex" at a real word boundary - the name heuristic alone would
     * correctly flag it. But it also has an inferred type here, purely from its initializer's
     * method-name suffix ("helper.getResultList()" ends in "List") - the lowest-trust
     * NAME_HEURISTIC source, same one isCollection/isO1 already refuse to act on. Before this
     * fix, a raw typeOf().simpleName read that irrelevant "List" guess as if it were a real
     * declared type, found it doesn't match regexTypes, and returned false WITHOUT ever
     * reaching the name check below it - silently swallowing a real regex-compile finding.
     */
    @Test
    fun `a regex-named variable with only a name-heuristic-inferred type is still flagged`() {
        val varName = "myRegex"
        val listyInit = FunctionCall("getResultList", "helper", emptyList(), loc, emptyList())
        val varDecl = VariableDecl(varName, null, initializer = listyInit, location = loc, children = listOf(listyInit))
        val arg = GenericNode("\"[0-9]+\"", loc, emptyList())
        val call = FunctionCall("compile", varName, listOf(arg), loc, emptyList())
        val callback = LoopNode(kind = LoopKind.HIGHER_ORDER, iteratedVariable = "items", location = loc, children = listOf(call))
        findingsForCallbackWithVarDecl(callback, varDecl) shouldHaveSize 1
    }

    private fun findingsForCallbackWithVarDecl(
        callback: LoopNode,
        varDecl: VariableDecl,
    ): List<Finding> {
        val fn =
            FunctionDecl(
                name = "process",
                qualifiedName = "Fixture.process",
                parameters = emptyList(),
                declaringClass = "Fixture",
                location = loc,
                children = listOf(varDecl, callback),
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

    private fun findingsForWithDeclaredType(
        varName: String,
        declaredType: String,
    ): List<Finding> {
        val arg = GenericNode("\"[0-9]+\"", loc, emptyList())
        val call = FunctionCall("compile", varName, listOf(arg), loc, emptyList())
        val callback = LoopNode(kind = LoopKind.HIGHER_ORDER, iteratedVariable = "items", location = loc, children = listOf(call))
        return findingsForCallbackWithDeclaredType(callback, varName, declaredType)
    }

    private fun findingsForCallbackWithDeclaredType(
        callback: LoopNode,
        varName: String,
        declaredType: String,
    ): List<Finding> {
        val fn =
            FunctionDecl(
                name = "process",
                qualifiedName = "Fixture.process",
                parameters = listOf(Parameter(varName, declaredType)),
                declaringClass = "Fixture",
                location = loc,
                children = listOf(callback),
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

    private fun findingsFor(qualifiedTarget: String): List<Finding> {
        val arg = GenericNode("\"[0-9]+\"", loc, emptyList())
        val call = FunctionCall("compile", qualifiedTarget, listOf(arg), loc, emptyList())
        val callback = LoopNode(kind = LoopKind.HIGHER_ORDER, iteratedVariable = "items", location = loc, children = listOf(call))
        val fileRoot = FileRoot(filePath = "Fixture.java", language = Language.JAVA, location = loc, children = listOf(callback))
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
