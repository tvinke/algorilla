package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.SourceLocation
import com.github.tvinke.algorilla.semantics.LanguageSemanticsRegistry
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

/**
 * Canary property for isDateType/isDateParseCall - found during the cc#105 bare-.contains()
 * sweep. Both did a plain typeName.contains(it)/target.contains(it) against date-type-names
 * (Date, LocalDate, Instant, ...), no boundary check either side. Case-sensitivity already
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
}
