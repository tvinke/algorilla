package com.github.tvinke.algorilla.engine

import com.github.tvinke.algorilla.engine.demoteLifecycleFindings
import com.github.tvinke.algorilla.model.ClassNode
import com.github.tvinke.algorilla.model.Confidence
import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.model.SourceLocation
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.semantics.LanguageSemanticsRegistry
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class LifecycleDemotionTest {
    private val registry = LanguageSemanticsRegistry.loadDefaults()
    private val loc = SourceLocation("Test.java", 1, 1)

    private fun finding(
        line: Int,
        confidence: Confidence = Confidence.MEDIUM,
    ) = Finding(
        ruleId = "io-in-loop",
        ruleName = "IO in Loop",
        severity = Severity.WARNING,
        location = SourceLocation("Test.java", line, 1),
        message = "test",
        suggestions = emptyList(),
        confidence = confidence,
        evidence = emptyList(),
    )

    private fun fn(
        name: String,
        line: Int,
        endLine: Int,
        annotations: List<String> = emptyList(),
        declaringClass: String? = null,
    ) = FunctionDecl(
        name = name,
        qualifiedName = "Test.$name",
        parameters = emptyList(),
        annotations = annotations,
        declaringClass = declaringClass,
        location = SourceLocation("Test.java", line, 1),
        children =
            listOf(
                FunctionDecl(
                    name = "inner",
                    qualifiedName = "inner",
                    parameters = emptyList(),
                    location = SourceLocation("Test.java", endLine, 1),
                    children = emptyList(),
                ),
            ),
    )

    @Nested
    inner class MethodAnnotationDemotion {
        @Test
        fun `should demote findings inside @PostConstruct method to LOW`() {
            val initFn = fn("initialize", 10, 20, annotations = listOf("PostConstruct"))
            val fileRoot = FileRoot("Test.java", Language.JAVA, loc, listOf(initFn))
            val findings = listOf(finding(15, Confidence.MEDIUM))

            val result = demoteLifecycleFindings(findings, mapOf("Test.java" to fileRoot), registry)

            result.first().confidence shouldBe Confidence.LOW
        }

        @Test
        fun `should demote findings inside @Bean method to LOW`() {
            val beanFn = fn("createService", 10, 20, annotations = listOf("Bean"))
            val fileRoot = FileRoot("Test.java", Language.JAVA, loc, listOf(beanFn))
            val findings = listOf(finding(15, Confidence.HIGH))

            val result = demoteLifecycleFindings(findings, mapOf("Test.java" to fileRoot), registry)

            result.first().confidence shouldBe Confidence.LOW
        }

        @Test
        fun `should NOT demote findings in unannotated methods`() {
            val regularFn = fn("processRequest", 10, 20)
            val fileRoot = FileRoot("Test.java", Language.JAVA, loc, listOf(regularFn))
            val findings = listOf(finding(15, Confidence.MEDIUM))

            val result = demoteLifecycleFindings(findings, mapOf("Test.java" to fileRoot), registry)

            result.first().confidence shouldBe Confidence.MEDIUM
        }

        @Test
        fun `should NOT demote findings for non-lifecycle annotations`() {
            val overrideFn = fn("toString", 10, 20, annotations = listOf("Override"))
            val fileRoot = FileRoot("Test.java", Language.JAVA, loc, listOf(overrideFn))
            val findings = listOf(finding(15, Confidence.MEDIUM))

            val result = demoteLifecycleFindings(findings, mapOf("Test.java" to fileRoot), registry)

            result.first().confidence shouldBe Confidence.MEDIUM
        }
    }

    @Nested
    inner class InterfaceCallbackDemotion {
        @Test
        fun `should demote findings inside afterPropertiesSet when class implements InitializingBean`() {
            val afterPropsFn = fn("afterPropertiesSet", 10, 20, declaringClass = "MyService")
            val classNode =
                ClassNode(
                    name = "MyService",
                    supertypes = listOf("InitializingBean"),
                    location = loc,
                    children = listOf(afterPropsFn),
                )
            val fileRoot = FileRoot("Test.java", Language.JAVA, loc, listOf(classNode))
            val findings = listOf(finding(15, Confidence.MEDIUM))

            val result = demoteLifecycleFindings(findings, mapOf("Test.java" to fileRoot), registry)

            result.first().confidence shouldBe Confidence.LOW
        }

        @Test
        fun `should NOT demote regular methods even when class implements InitializingBean`() {
            val regularFn = fn("processRequest", 30, 40, declaringClass = "MyService")
            val afterPropsFn = fn("afterPropertiesSet", 10, 20, declaringClass = "MyService")
            val classNode =
                ClassNode(
                    name = "MyService",
                    supertypes = listOf("InitializingBean"),
                    location = loc,
                    children = listOf(afterPropsFn, regularFn),
                )
            val fileRoot = FileRoot("Test.java", Language.JAVA, loc, listOf(classNode))
            val findings = listOf(finding(35, Confidence.MEDIUM))

            val result = demoteLifecycleFindings(findings, mapOf("Test.java" to fileRoot), registry)

            result.first().confidence shouldBe Confidence.MEDIUM
        }
    }
}
