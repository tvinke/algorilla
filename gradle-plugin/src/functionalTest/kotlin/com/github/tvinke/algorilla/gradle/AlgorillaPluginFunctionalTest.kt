package com.github.tvinke.algorilla.gradle

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class AlgorillaPluginFunctionalTest {
    @TempDir
    internal lateinit var projectDir: File

    private fun setupProject(
        buildExtra: String = "",
        sourceFile: String? = null,
    ) {
        projectDir.resolve("settings.gradle.kts").writeText("rootProject.name = \"test-project\"")
        projectDir.resolve("build.gradle.kts").writeText(
            """
            plugins {
                java
                id("com.github.tvinke.algorilla")
            }

            $buildExtra
            """.trimIndent(),
        )

        if (sourceFile != null) {
            val srcDir = projectDir.resolve("src/main/java/com/example")
            srcDir.mkdirs()
            srcDir.resolve("Example.java").writeText(sourceFile)
        }
    }

    private fun runner(vararg args: String) =
        GradleRunner
            .create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments(*args)

    @Test
    internal fun `plugin registers algorilla task`() {
        setupProject()
        val result = runner("tasks", "--group=verification").build()
        result.output shouldContain "algorilla"
    }

    @Test
    internal fun `task succeeds on empty project`() {
        setupProject()
        val result = runner("algorilla").build()
        result.task(":algorilla")?.outcome shouldBe TaskOutcome.SUCCESS
    }

    @Test
    internal fun `task produces report file`() {
        setupProject(
            sourceFile =
                """
                package com.example;
                public class Example {
                    public String hello() { return "world"; }
                }
                """.trimIndent(),
        )
        runner("algorilla").build()
        val report = projectDir.resolve("build/reports/algorilla.json")
        report.exists() shouldBe true
        report.readText() shouldContain "totalFindings"
    }

    @Test
    internal fun `custom failOn=error does not fail on warnings`() {
        setupProject(
            buildExtra =
                """
                algorilla {
                    failOn.set("error")
                }
                """.trimIndent(),
            sourceFile =
                """
                package com.example;
                public class Example {
                    public String hello() { return "world"; }
                }
                """.trimIndent(),
        )
        val result = runner("algorilla").build()
        result.task(":algorilla")?.outcome shouldBe TaskOutcome.SUCCESS
    }

    @Test
    internal fun `rule filter limits which rules run`() {
        setupProject(
            buildExtra =
                """
                algorilla {
                    rules.set(listOf("nested-lookup"))
                }
                """.trimIndent(),
            sourceFile =
                """
                package com.example;
                public class Example {
                    public String hello() { return "world"; }
                }
                """.trimIndent(),
        )
        val result = runner("algorilla").build()
        result.task(":algorilla")?.outcome shouldBe TaskOutcome.SUCCESS
    }
}
