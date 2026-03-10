package com.github.tvinke.algorilla.cli

import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class ProjectStructureDetectorTest {
    private val detector = ProjectStructureDetector()

    @Nested
    inner class ResolveProjectRoot {
        @Test
        fun `should detect Gradle project root from build file`(
            @TempDir tmp: File,
        ) {
            File(tmp, "build.gradle.kts").createNewFile()

            detector.resolveProjectRoot(tmp) shouldBe tmp
        }

        @Test
        fun `should detect Gradle project root from settings file`(
            @TempDir tmp: File,
        ) {
            File(tmp, "settings.gradle.kts").createNewFile()

            detector.resolveProjectRoot(tmp) shouldBe tmp
        }

        @Test
        fun `should detect Maven project root from pom`(
            @TempDir tmp: File,
        ) {
            File(tmp, "pom.xml").writeText("<project/>")

            detector.resolveProjectRoot(tmp) shouldBe tmp
        }

        @Test
        fun `should detect JS project root from package json`(
            @TempDir tmp: File,
        ) {
            File(tmp, "package.json").writeText("{}")

            detector.resolveProjectRoot(tmp) shouldBe tmp
        }

        @Test
        fun `should walk up from subdirectory to find project root`(
            @TempDir tmp: File,
        ) {
            File(tmp, "build.gradle.kts").createNewFile()
            val sub = File(tmp, "src/main/java").apply { mkdirs() }

            detector.resolveProjectRoot(sub) shouldBe tmp
        }

        @Test
        fun `should return given dir when no build system detected`(
            @TempDir tmp: File,
        ) {
            detector.resolveProjectRoot(tmp) shouldBe tmp.canonicalFile
        }
    }

    @Nested
    inner class ResolveSourceRootsGradle {
        @Test
        fun `should detect single-module Gradle source dirs`(
            @TempDir tmp: File,
        ) {
            File(tmp, "build.gradle.kts").createNewFile()
            File(tmp, "src/main/java").mkdirs()
            File(tmp, "src/main/kotlin").mkdirs()

            val roots = detector.resolveSourceRoots(tmp, tmp)

            roots shouldContainExactlyInAnyOrder
                listOf(
                    File(tmp, "src/main/java").canonicalFile,
                    File(tmp, "src/main/kotlin").canonicalFile,
                )
        }

        @Test
        fun `should detect multi-module Gradle source dirs`(
            @TempDir tmp: File,
        ) {
            File(tmp, "settings.gradle.kts").writeText(
                """
                include("core")
                include("cli")
                """.trimIndent(),
            )
            File(tmp, "build.gradle.kts").createNewFile()
            File(tmp, "core/src/main/java").mkdirs()
            File(tmp, "core/src/main/kotlin").mkdirs()
            File(tmp, "cli/src/main/kotlin").mkdirs()

            val roots = detector.resolveSourceRoots(tmp, tmp)

            roots shouldContainExactlyInAnyOrder
                listOf(
                    File(tmp, "core/src/main/java").canonicalFile,
                    File(tmp, "core/src/main/kotlin").canonicalFile,
                    File(tmp, "cli/src/main/kotlin").canonicalFile,
                )
        }

        @Test
        fun `should detect modules from multi-arg include call`(
            @TempDir tmp: File,
        ) {
            File(tmp, "settings.gradle.kts").writeText(
                """
                include("core", "cli", "api")
                """.trimIndent(),
            )
            File(tmp, "build.gradle.kts").createNewFile()
            File(tmp, "core/src/main/kotlin").mkdirs()
            File(tmp, "cli/src/main/kotlin").mkdirs()
            File(tmp, "api/src/main/java").mkdirs()

            val roots = detector.resolveSourceRoots(tmp, tmp)

            roots shouldContainExactlyInAnyOrder
                listOf(
                    File(tmp, "core/src/main/kotlin").canonicalFile,
                    File(tmp, "cli/src/main/kotlin").canonicalFile,
                    File(tmp, "api/src/main/java").canonicalFile,
                )
        }

        @Test
        fun `should strip colon prefix from module names`(
            @TempDir tmp: File,
        ) {
            File(tmp, "settings.gradle.kts").writeText(
                """
                include(":core", ":cli")
                """.trimIndent(),
            )
            File(tmp, "build.gradle.kts").createNewFile()
            File(tmp, "core/src/main/java").mkdirs()
            File(tmp, "cli/src/main/kotlin").mkdirs()

            val roots = detector.resolveSourceRoots(tmp, tmp)

            roots shouldContainExactlyInAnyOrder
                listOf(
                    File(tmp, "core/src/main/java").canonicalFile,
                    File(tmp, "cli/src/main/kotlin").canonicalFile,
                )
        }

        @Test
        fun `should detect modules from Groovy settings file`(
            @TempDir tmp: File,
        ) {
            File(tmp, "settings.gradle").writeText(
                """
                include ':core', ':cli'
                """.trimIndent(),
            )
            File(tmp, "build.gradle").createNewFile()
            File(tmp, "core/src/main/java").mkdirs()
            File(tmp, "cli/src/main/groovy").mkdirs()

            val roots = detector.resolveSourceRoots(tmp, tmp)

            roots shouldContainExactlyInAnyOrder
                listOf(
                    File(tmp, "core/src/main/java").canonicalFile,
                    File(tmp, "cli/src/main/groovy").canonicalFile,
                )
        }

        @Test
        fun `should handle hierarchical module paths`(
            @TempDir tmp: File,
        ) {
            File(tmp, "settings.gradle.kts").writeText(
                """
                include("core:model")
                include("core:api")
                """.trimIndent(),
            )
            File(tmp, "build.gradle.kts").createNewFile()
            File(tmp, "core/model/src/main/kotlin").mkdirs()
            File(tmp, "core/api/src/main/java").mkdirs()

            val roots = detector.resolveSourceRoots(tmp, tmp)

            roots shouldContainExactlyInAnyOrder
                listOf(
                    File(tmp, "core/model/src/main/kotlin").canonicalFile,
                    File(tmp, "core/api/src/main/java").canonicalFile,
                )
        }

        @Test
        fun `should fall back to project root when no source dirs exist`(
            @TempDir tmp: File,
        ) {
            File(tmp, "settings.gradle.kts").writeText("""include("api")""")
            File(tmp, "build.gradle.kts").createNewFile()

            val roots = detector.resolveSourceRoots(tmp, tmp)

            roots shouldBe listOf(tmp.canonicalFile)
        }

        @Test
        fun `should include groovy source dirs`(
            @TempDir tmp: File,
        ) {
            File(tmp, "build.gradle.kts").createNewFile()
            File(tmp, "src/main/groovy").mkdirs()

            val roots = detector.resolveSourceRoots(tmp, tmp)

            roots shouldHaveSize 1
            roots.first().name shouldBe "groovy"
        }
    }

    @Nested
    inner class ResolveSourceRootsMaven {
        @Test
        fun `should detect single-module Maven source dir`(
            @TempDir tmp: File,
        ) {
            File(tmp, "pom.xml").writeText("<project/>")
            File(tmp, "src/main/java").mkdirs()

            val roots = detector.resolveSourceRoots(tmp, tmp)

            roots shouldBe listOf(File(tmp, "src/main/java").canonicalFile)
        }

        @Test
        fun `should detect multi-module Maven source dirs`(
            @TempDir tmp: File,
        ) {
            File(tmp, "pom.xml").writeText(
                """
                <project>
                  <modules>
                    <module>api</module>
                    <module>service</module>
                  </modules>
                </project>
                """.trimIndent(),
            )
            File(tmp, "api/src/main/java").mkdirs()
            File(tmp, "service/src/main/java").mkdirs()

            val roots = detector.resolveSourceRoots(tmp, tmp)

            roots shouldContainExactlyInAnyOrder
                listOf(
                    File(tmp, "api/src/main/java").canonicalFile,
                    File(tmp, "service/src/main/java").canonicalFile,
                )
        }
    }

    @Nested
    inner class ResolveSourceRootsJsTs {
        @Test
        fun `should return project root for JS projects`(
            @TempDir tmp: File,
        ) {
            File(tmp, "package.json").writeText("{}")

            val roots = detector.resolveSourceRoots(tmp, tmp)

            roots shouldBe listOf(tmp.canonicalFile)
        }
    }

    @Nested
    inner class TargetedScan {
        @Test
        fun `should return explicit subdirectory for targeted scan`(
            @TempDir tmp: File,
        ) {
            File(tmp, "build.gradle.kts").createNewFile()
            val sub = File(tmp, "src/main/java").apply { mkdirs() }

            val roots = detector.resolveSourceRoots(tmp, sub)

            roots shouldBe listOf(sub.canonicalFile)
        }
    }

    @Nested
    inner class FallbackBehavior {
        @Test
        fun `should return given dir when no build system detected`(
            @TempDir tmp: File,
        ) {
            val roots =
                detector.resolveSourceRoots(tmp.canonicalFile, tmp)

            roots shouldBe listOf(tmp.canonicalFile)
        }
    }

    @Nested
    inner class TestExclusion {
        @Test
        fun `should exclude test dirs for Gradle project`(
            @TempDir tmp: File,
        ) {
            File(tmp, "build.gradle.kts").createNewFile()
            val mainFile =
                File(tmp, "src/main/java/App.java").apply {
                    parentFile.mkdirs()
                    createNewFile()
                }
            val testFile =
                File(tmp, "src/test/java/AppTest.java").apply {
                    parentFile.mkdirs()
                    createNewFile()
                }

            val filter =
                detector.buildTestExcludeFilter(listOf(tmp), includeTests = false)

            filter(mainFile) shouldBe true
            filter(testFile) shouldBe false
        }

        @Test
        fun `should include test dirs when flag is set`(
            @TempDir tmp: File,
        ) {
            File(tmp, "build.gradle.kts").createNewFile()
            val testFile =
                File(tmp, "src/test/java/AppTest.java").apply {
                    parentFile.mkdirs()
                    createNewFile()
                }

            val filter =
                detector.buildTestExcludeFilter(listOf(tmp), includeTests = true)

            filter(testFile) shouldBe true
        }
    }

    @Nested
    inner class DefaultExcludes {
        @Test
        fun `should exclude algorilla cache directory`() {
            detector.defaultExcludePatterns() shouldContain "/.algorilla/"
        }
    }
}
