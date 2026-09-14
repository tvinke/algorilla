package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.Parameter
import com.github.tvinke.algorilla.model.SourceLocation
import com.github.tvinke.algorilla.model.VariableDecl
import com.github.tvinke.algorilla.semantics.LanguageSemanticsRegistry
import com.github.tvinke.algorilla.semantics.TypeEnvironment
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

/**
 * Canary property for isDateType/isDateParseCall - a later sweep for the same
 * bare-.contains() pattern. Both did a plain typeName.contains(it)/target.contains(it)
 * against date-type-names (Date, LocalDate, Instant, ...), no boundary check either
 * side. Case-sensitivity already
 * protected most of the obvious false positives ("update"/"candidate" have a lowercase "date",
 * not the capitalized type name), but a class like "Dateline" - a real, unrelated concept,
 * not a date value - has a genuinely capitalized "Date" with no boundary at all on the far
 * side, and got misread as a date type.
 */
internal class ExpensiveSortComparatorRuleNameVsTypeTest {
    private val loc = SourceLocation("Fixture.java", 1, 1)
    private val registry = LanguageSemanticsRegistry.loadDefaults()

    @Test
    fun `a real Date-typed comparator field is still recognized`() {
        isDateType("Date", Language.JAVA, registry) shouldBe true
        isDateType("LocalDateTime", Language.JAVA, registry) shouldBe true
    }

    @Test
    fun `a class merely starting with Date but continuing the same word is not misread as a date type`() {
        isDateType("Dateline", Language.JAVA, registry) shouldBe false
    }

    @Test
    fun `a real date-parse call target is still recognized`() {
        val call = FunctionCall("parse", "LocalDate", emptyList(), loc, emptyList())
        isDateParseCall(call, Language.JAVA, registry) shouldBe true
    }

    /**
     * Documented gap, not fixed here: a boundary check alone can't tell "DateUtils"/
     * "InstantSource" (real, unrelated utility classes) apart from a genuine compound date
     * type - both have a capitalized continuation right after the match, same ambiguity as
     * "ResultSet"/"InputStream" in LanguageSemanticsRegistry's isO1Type/isCollectionType.
     * That one got a YAML exclusion list because its blast radius (every rule that calls
     * isCollection/isO1) justified it; this rule's date-type check is narrower (comparator
     * cost estimation only), so it's pinned as a known limitation instead of adding a third
     * exclusion-list YAML section for a lower-impact heuristic.
     */
    @Test
    fun `a utility class with a capitalized continuation right after the date type name is still misread (documented gap)`() {
        isDateType("DateUtils", Language.JAVA, registry) shouldBe true
    }

    /**
     * A static factory call's qualifiedTarget is already the real type name (LocalDate.parse),
     * nothing to check further. But an instance call's qualifiedTarget is a *variable* name -
     * "myDateTimeFormatterHelper" still boundary-matches "DateTimeFormatter" by name, even
     * though its declared type here is an unrelated formatter. With a TypeEnvironment
     * available, the declared type wins.
     */
    @Test
    fun `a variable named like a date-parse target but declared as something else is not flagged`() {
        val call = FunctionCall("parse", "myDateTimeFormatterHelper", emptyList(), loc, emptyList())
        val typeEnv = typeEnvFor("myDateTimeFormatterHelper", "CustomFormatter")
        isDateParseCall(call, Language.JAVA, registry, typeEnv) shouldBe false
    }

    @Test
    fun `a variable not named like a date-parse target but declared as SimpleDateFormat is still flagged`() {
        val call = FunctionCall("parse", "fmt", emptyList(), loc, emptyList())
        val typeEnv = typeEnvFor("fmt", "SimpleDateFormat")
        isDateParseCall(call, Language.JAVA, registry, typeEnv) shouldBe true
    }

    /**
     * "myDateTimeFormatterHelper" boundary-matches "DateTimeFormatter" by name - the name
     * heuristic alone would correctly flag it. But its only type evidence here comes from an
     * initializer's method-name suffix ("helper.getResultList()" ends in "List") - the
     * lowest-trust NAME_HEURISTIC source, same one isCollection/isO1 already refuse to act
     * on. A raw typeOf().simpleName read that irrelevant "List" guess as a real declared
     * type, found it doesn't match dateParseTargets, and returned false without ever
     * reaching the name check - silently swallowing a real date-parse-in-comparator finding.
     */
    @Test
    fun `a date-parse-target-named variable with only a name-heuristic-inferred type is still flagged`() {
        val call = FunctionCall("parse", "myDateTimeFormatterHelper", emptyList(), loc, emptyList())
        val typeEnv = typeEnvWithNameHeuristicType("myDateTimeFormatterHelper")
        isDateParseCall(call, Language.JAVA, registry, typeEnv) shouldBe true
    }

    private fun typeEnvWithNameHeuristicType(varName: String): TypeEnvironment {
        val listyInit = FunctionCall("getResultList", "helper", emptyList(), loc, emptyList())
        val varDecl = VariableDecl(varName, null, initializer = listyInit, location = loc, children = listOf(listyInit))
        val fn =
            FunctionDecl(
                name = "compare",
                qualifiedName = "Fixture.compare",
                parameters = emptyList(),
                declaringClass = "Fixture",
                location = loc,
                children = listOf(varDecl),
            )
        return TypeEnvironment.build(fn, emptyMap(), Language.JAVA, registry)
    }

    private fun typeEnvFor(
        varName: String,
        declaredType: String,
    ): TypeEnvironment {
        val fn =
            FunctionDecl(
                name = "compare",
                qualifiedName = "Fixture.compare",
                parameters = listOf(Parameter(varName, declaredType)),
                declaringClass = "Fixture",
                location = loc,
                children = emptyList(),
            )
        return TypeEnvironment.build(fn, emptyMap(), Language.JAVA, registry)
    }
}
