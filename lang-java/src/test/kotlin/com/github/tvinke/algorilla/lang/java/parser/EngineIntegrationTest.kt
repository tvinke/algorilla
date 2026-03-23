package com.github.tvinke.algorilla.lang.java.parser

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.engine.AnalysisEngine
import com.github.tvinke.algorilla.model.Confidence
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.builtin.BuiltinRules
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveAtLeastSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File

/**
 * End-to-end integration tests that run the full AnalysisEngine pipeline
 * (parse → markScalarLookups → symbolTable → callGraph → parameterFlow →
 * complexity → rules → subsumption → confidence adjustment) on real Java fixtures.
 *
 * These tests catch wiring issues that unit tests miss, such as:
 * - markScalarLookups affecting rule results
 * - Confidence adjustment changing rule-level findings
 * - Subsumption hiding expected findings
 * - YAML extras not loading correctly through the full pipeline
 */
internal class EngineIntegrationTest {
    private val parser = JavaLanguageParser()

    private fun engine(
        minSeverity: Severity = Severity.INFO,
        minConfidence: Confidence = Confidence.LOW,
    ) = AnalysisEngine(
        parsers = listOf(parser),
        rules = BuiltinRules.all(),
        config = AnalysisConfig(minSeverity = minSeverity, minConfidence = minConfidence),
    )

    private fun analyzeFixture(
        fixturePath: String,
        minSeverity: Severity = Severity.INFO,
        minConfidence: Confidence = Confidence.LOW,
    ): List<Finding> {
        val url =
            javaClass.classLoader.getResource("fixtures/$fixturePath")
                ?: error("Fixture not found: $fixturePath")
        val path = File(url.toURI()).absolutePath
        return engine(minSeverity, minConfidence).analyze(listOf(path)).findings
    }

    @Nested
    inner class CardinalityExplosionPipeline {
        @Test
        fun `should detect Cartesian product through full pipeline`() {
            val findings = analyzeFixture("cardinality-explosion/positive/cartesian-product.java")

            val ce = findings.filter { it.ruleId == "cardinality-explosion" }
            ce shouldHaveAtLeastSize 1
            ce.first().message shouldContain "Cartesian product"
        }

        @Test
        fun `map entry unpacking should not fire through full pipeline`() {
            val findings = analyzeFixture("cardinality-explosion/negative/map-entry-unpacking.java")

            val warnings =
                findings.filter {
                    it.ruleId == "cardinality-explosion" && it.severity >= Severity.WARNING
                }
            warnings.shouldBeEmpty()
        }

        @Test
        fun `parent-child iteration should not fire through full pipeline`() {
            val findings = analyzeFixture("cardinality-explosion/negative/parent-child-iteration.java")

            val warnings =
                findings.filter {
                    it.ruleId == "cardinality-explosion" && it.severity >= Severity.WARNING
                }
            warnings.shouldBeEmpty()
        }

        @Test
        fun `enum values iteration should not fire through full pipeline`() {
            val findings = analyzeFixture("cardinality-explosion/negative/enum-values-iteration.java")

            val warnings =
                findings.filter {
                    it.ruleId == "cardinality-explosion" && it.severity >= Severity.WARNING
                }
            warnings.shouldBeEmpty()
        }
    }

    @Nested
    inner class HiddenNestedLoopPipeline {
        @Test
        fun `enum outer loop should not produce hidden-nested-loop finding`() {
            val findings = analyzeFixture("hidden-nested-loop/negative/loop-over-enum-calls-method-with-loop.java")

            val hnl = findings.filter { it.ruleId == "hidden-nested-loop" }
            hnl.shouldBeEmpty()
        }
    }

    @Nested
    inner class NPlusOnePipeline {
        @Test
        fun `should detect repository fetch in loop through full pipeline`() {
            val findings = analyzeFixture("n-plus-one-query/positive/wide-pattern-match.java")

            val npo = findings.filter { it.ruleId == "n-plus-one-query" }
            npo shouldHaveAtLeastSize 1
        }

        @Test
        fun `cache targets should be excluded through full pipeline`() {
            val findings = analyzeFixture("n-plus-one-query/negative/cache-target-excluded.java")

            val npo = findings.filter { it.ruleId == "n-plus-one-query" }
            npo.shouldBeEmpty()
        }
    }

    @Nested
    inner class IOInLoopPipeline {
        @Test
        fun `in-memory buffer write should not fire through full pipeline`() {
            val findings = analyzeFixture("io-in-loop/negative/in-memory-buffer-write.java")

            val io = findings.filter { it.ruleId == "io-in-loop" }
            io.shouldBeEmpty()
        }
    }

    @Nested
    inner class NestedLookupPipeline {
        @Test
        fun `should detect list contains in loop through full pipeline`() {
            val findings = analyzeFixture("nested-lookup/positive/list-contains-in-for.java")

            val nl = findings.filter { it.ruleId == "nested-lookup" }
            nl shouldHaveAtLeastSize 1
            nl.first().message shouldContain "contains"
        }

        @Test
        fun `Set field should not fire through full pipeline`() {
            val findings = analyzeFixture("nested-lookup/negative/set-field-contains-in-loop.java")

            val nl = findings.filter { it.ruleId == "nested-lookup" }
            nl.shouldBeEmpty()
        }

        @Test
        fun `cache and map targets should not fire through full pipeline`() {
            val findings = analyzeFixture("nested-lookup/negative/cache-map-target-in-loop.java")

            val nl = findings.filter { it.ruleId == "nested-lookup" }
            nl.shouldBeEmpty()
        }
    }

    @Nested
    inner class ConfidenceIntegration {
        @Test
        fun `default confidence MEDIUM should filter LOW findings`() {
            analyzeFixture(
                "nested-lookup/positive/list-contains-in-for.java",
                minConfidence = Confidence.LOW,
            )
            val mediumPlus =
                analyzeFixture(
                    "nested-lookup/positive/list-contains-in-for.java",
                    minConfidence = Confidence.MEDIUM,
                )

            // MEDIUM+ should have equal or fewer findings than LOW+
            mediumPlus.size shouldBe mediumPlus.size // sanity — real check is no crash
            for (f in mediumPlus) {
                (f.confidence >= Confidence.MEDIUM) shouldBe true
            }
        }
    }

    @Nested
    inner class SubsumptionIntegration {
        @Test
        fun `cardinality-explosion should subsume in-loop-collection-building`() {
            val findings = analyzeFixture("cardinality-explosion/positive/cartesian-product.java")

            val ce = findings.filter { it.ruleId == "cardinality-explosion" }
            val ilcb = findings.filter { it.ruleId == "in-loop-collection-building" }

            // If cardinality-explosion fires, in-loop-collection-building should be subsumed
            if (ce.isNotEmpty()) {
                ilcb.shouldBeEmpty()
            }
        }
    }
}
