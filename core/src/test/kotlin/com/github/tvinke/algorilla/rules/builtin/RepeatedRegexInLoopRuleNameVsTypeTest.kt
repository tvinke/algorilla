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
import org.junit.jupiter.api.Test

/**
 * Canary properties for the testharnas campaign - this is the file the campaign
 * is named after: isCompileCall's `call.qualifiedTarget?.contains("Pattern")` is the exact
 * "receiver-text-as-type-proof" shape from the root-cause analysis behind ExpensiveCallbackRule's
 * `qualifiedTarget?.contains("Pattern")`/`contains("Regex")` checks, same file family.
 * `contains` matches anywhere in the string, so a totally unrelated class whose name
 * merely contains "Pattern"/"Regex" - `DateTimePatternValidator`, `NamingRegexResolver` -
 * gets its own, unrelated `compile()` method misread as `java.util.regex.Pattern.compile()`.
 */
internal class RepeatedRegexInLoopRuleNameVsTypeTest {
    private val loc = SourceLocation("Fixture.java", 1, 1)
    private val rule = RepeatedRegexInLoopRule()

    private val unrelatedClassesContainingRegexWords =
        listOf(
            "DateTimePatternValidator",
            "NamingRegexResolver",
            "PatternMatcherService",
        )

    @Test
    fun `a class whose name merely contains 'Pattern' or 'Regex' does not trigger a false regex-compile finding`() {
        unrelatedClassesContainingRegexWords.forEach { target ->
            findingsFor(target).shouldBeEmpty()
        }
    }

    @Test
    fun `a real Pattern-compile call is still flagged`() {
        findingsFor("Pattern") shouldHaveSize 1
    }

    @Test
    fun `a fully-qualified Pattern reference is still flagged`() {
        findingsFor("java.util.regex.Pattern") shouldHaveSize 1
    }

    @Test
    fun `a real Regex-compile call is still flagged`() {
        findingsFor("Regex") shouldHaveSize 1
    }

    /**
     * hasConstantArgument's ALL_CAPS-or-qualified check (`text.all { it == '_' ||
     * it.isUpperCase() || it == '.' }`) is a shape heuristic too, but a narrower one: it
     * only ever promotes a finding's confidence signal (constant vs. per-iteration
     * argument), not a name-vs-type classification, and every character in a real
     * constant reference like "MY_PATTERN" or "FOO.PATTERN" already satisfies it by
     * construction. No counter-example found worth pinning as a bug; left as-is.
     */
    @Test
    fun `a qualified constant reference is still recognized as hoistable`() {
        val arg = GenericNode("FOO.PATTERN", loc, emptyList())
        val call = FunctionCall("compile", "Pattern", listOf(arg), loc, emptyList())
        val loop = LoopNode(kind = LoopKind.FOR_EACH, iteratedVariable = "items", location = loc, children = listOf(call))
        val fileRoot = FileRoot(filePath = "Fixture.java", language = Language.JAVA, location = loc, children = listOf(loop))
        val context =
            AnalysisContext(
                irTrees = mapOf("Fixture.java" to fileRoot),
                symbolTable = SymbolTable(),
                callGraph = CallGraph(),
                config = AnalysisConfig(),
            )
        rule.evaluate(context) shouldHaveSize 1
    }

    /**
     * "userPattern" ends with "Pattern" at a real word boundary, so the name heuristic alone
     * would still misread this as a regex compile - but its declared type is a domain class
     * that merely happens to have its own compile() method. With a TypeEnvironment available,
     * the declared type wins over the name.
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
     * "myRegex" ends with "Regex" at a real word boundary - the name heuristic alone would
     * correctly flag it. But its only type evidence here comes from an initializer's
     * method-name suffix ("helper.getResultList()" ends in "List") - the lowest-trust
     * NAME_HEURISTIC source, same one isCollection/isO1 already refuse to act on. A raw
     * typeOf().simpleName read that irrelevant "List" guess as if it were a real declared
     * type, found it doesn't match regexTypes, and returned false without ever reaching the
     * name check - silently swallowing a real regex-compile-in-loop finding.
     */
    @Test
    fun `a regex-named variable with only a name-heuristic-inferred type is still flagged`() {
        findingsForWithNameHeuristicType("myRegex") shouldHaveSize 1
    }

    private fun findingsForWithNameHeuristicType(varName: String): List<Finding> {
        val listyInit = FunctionCall("getResultList", "helper", emptyList(), loc, emptyList())
        val varDecl = VariableDecl(varName, null, initializer = listyInit, location = loc, children = listOf(listyInit))
        val arg = GenericNode("\"[0-9]+\"", loc, emptyList())
        val call = FunctionCall("compile", varName, listOf(arg), loc, emptyList())
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
        return rule.evaluate(context)
    }

    private fun findingsForWithDeclaredType(
        varName: String,
        declaredType: String,
    ): List<Finding> {
        val arg = GenericNode("\"[0-9]+\"", loc, emptyList())
        val call = FunctionCall("compile", varName, listOf(arg), loc, emptyList())
        val loop = LoopNode(kind = LoopKind.FOR_EACH, iteratedVariable = "items", location = loc, children = listOf(call))
        val fn =
            FunctionDecl(
                name = "process",
                qualifiedName = "Fixture.process",
                parameters = listOf(Parameter(varName, declaredType)),
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

    private fun findingsFor(qualifiedTarget: String): List<Finding> {
        val arg = GenericNode("\"[0-9]+\"", loc, emptyList())
        val call = FunctionCall("compile", qualifiedTarget, listOf(arg), loc, emptyList())
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
