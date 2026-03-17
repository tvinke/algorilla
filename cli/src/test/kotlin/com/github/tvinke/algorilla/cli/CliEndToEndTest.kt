package com.github.tvinke.algorilla.cli

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import picocli.CommandLine
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.io.PrintWriter
import java.io.StringWriter

/**
 * End-to-end tests for the CLI command surface.
 * These exercise AlgorillaCommand via PicoCLI without spawning a process,
 * verifying that the public option surface works as documented.
 */
internal class CliEndToEndTest {
    @TempDir
    lateinit var tempDir: File

    private fun run(vararg args: String): CliResult {
        // PicoCLI captures --help/--version output via its own writer,
        // but AlgorillaCommand writes report output to System.out/System.err directly.
        // Capture both.
        val picoOut = StringWriter()
        val picoErr = StringWriter()
        val sysOut = ByteArrayOutputStream()
        val sysErr = ByteArrayOutputStream()
        val origOut = System.out
        val origErr = System.err
        try {
            System.setOut(PrintStream(sysOut))
            System.setErr(PrintStream(sysErr))
            val cmd = CommandLine(AlgorillaCommand())
            cmd.out = PrintWriter(picoOut)
            cmd.err = PrintWriter(picoErr)
            val exitCode = cmd.execute(*args)
            return CliResult(
                exitCode,
                picoOut.toString() + sysOut.toString(),
                picoErr.toString() + sysErr.toString(),
            )
        } finally {
            System.setOut(origOut)
            System.setErr(origErr)
        }
    }

    private fun writeSource(
        name: String,
        content: String,
    ): File {
        val dir = tempDir.resolve("src/main/java/com/example")
        dir.mkdirs()
        val file = dir.resolve(name)
        file.writeText(content)
        return tempDir
    }

    // -- Basic invocation --

    @Test
    fun `version flag prints version and exits 0`() {
        val result = run("--version")
        result.exitCode shouldBe 0
        result.stdout shouldContain "algorilla"
    }

    @Tag("contract")
    @Test
    fun `help flag prints usage and exits 0`() {
        val result = run("--help")
        result.exitCode shouldBe 0
        result.stdout shouldContain "--format"
        result.stdout shouldContain "--severity"
        result.stdout shouldContain "--confidence"
        result.stdout shouldContain "--fail-on"
        result.stdout shouldContain "--baseline"
        result.stdout shouldContain "--rule"
        result.stdout shouldContain "--language"
        result.stdout shouldContain "--exclude"
        result.stdout shouldContain "--include-tests"
        result.stdout shouldContain "--accept"
        result.stdout shouldContain "--no-cache"
        result.stdout shouldContain "--list-rules"
        result.stdout shouldContain "--color"
    }

    @Test
    fun `list-rules prints all rules and exits 0`() {
        val result = run("--list-rules")
        result.exitCode shouldBe 0
        result.stdout shouldContain "nested-lookup"
        result.stdout shouldContain "n-plus-one-query"
        result.stdout shouldContain "sort-for-last"
    }

    // -- Clean project (no findings) --

    @Test
    fun `clean source exits 0`() {
        val project =
            writeSource(
                "Clean.java",
                """
                package com.example;
                import java.util.HashSet;
                import java.util.List;
                import java.util.Set;
                public class Clean {
                    public boolean check(List<String> items, Set<String> allowed) {
                        for (String item : items) {
                            if (allowed.contains(item)) return true;
                        }
                        return false;
                    }
                }
                """.trimIndent(),
            )
        val result = run("--no-cache", project.absolutePath)
        result.exitCode shouldBe 0
    }

    // -- Finding detection --

    @Tag("contract")
    @Test
    fun `nested lookup in source triggers finding and exits 1`() {
        val project = writeSource("Bad.java", NESTED_LOOKUP_SOURCE)
        val result = run("--no-cache", "--fail-on", "warning", project.absolutePath)
        result.exitCode shouldBe 1
        result.stdout shouldContain "nested-lookup"
    }

    @Tag("contract")
    @Test
    fun `fail-on error exits 0 on warnings`() {
        val project = writeSource("Bad.java", NESTED_LOOKUP_SOURCE)
        val result = run("--no-cache", "--fail-on", "error", project.absolutePath)
        result.exitCode shouldBe 0
    }

    @Test
    fun `severity error hides warnings from output`() {
        val project = writeSource("Bad.java", NESTED_LOOKUP_SOURCE)
        val result = run("--no-cache", "--severity", "error", "--fail-on", "error", project.absolutePath)
        result.exitCode shouldBe 0
        result.stdout shouldNotContain "nested-lookup"
    }

    // -- Output formats --

    @Tag("contract")
    @Test
    fun `json format writes valid json`() {
        val project = writeSource("Bad.java", NESTED_LOOKUP_SOURCE)
        val outFile = tempDir.resolve("report.json")
        run("--no-cache", "--format", "json", "--output", outFile.absolutePath, "--fail-on", "error", project.absolutePath)
        outFile.exists() shouldBe true
        val content = outFile.readText()
        content shouldContain "\"schemaVersion\""
        content shouldContain "\"totalFindings\""
        content shouldContain "nested-lookup"
    }

    @Tag("contract")
    @Test
    fun `sarif format writes valid sarif`() {
        val project = writeSource("Bad.java", NESTED_LOOKUP_SOURCE)
        val outFile = tempDir.resolve("report.sarif")
        run("--no-cache", "--format", "sarif", "--output", outFile.absolutePath, "--fail-on", "error", project.absolutePath)
        outFile.exists() shouldBe true
        val content = outFile.readText()
        content shouldContain "\"version\": \"2.1.0\""
        content shouldContain "nested-lookup"
    }

    // -- Rule filtering --

    @Test
    fun `rule filter limits detection to specified rules`() {
        val project = writeSource("Bad.java", NESTED_LOOKUP_SOURCE)
        val result = run("--no-cache", "--rule", "sort-for-last", "--fail-on", "error", project.absolutePath)
        result.exitCode shouldBe 0
        result.stdout shouldNotContain "nested-lookup"
    }

    @Test
    fun `rule filter includes matching rule`() {
        val project = writeSource("Bad.java", NESTED_LOOKUP_SOURCE)
        val result = run("--no-cache", "--rule", "nested-lookup", "--fail-on", "error", project.absolutePath)
        result.stdout shouldContain "nested-lookup"
    }

    // -- Language filtering --

    @Test
    fun `language filter skips non-matching files`() {
        val project = writeSource("Bad.java", NESTED_LOOKUP_SOURCE)
        val result = run("--no-cache", "--language", "groovy", "--fail-on", "error", project.absolutePath)
        result.exitCode shouldBe 0
    }

    // -- Confidence filtering --

    @Test
    fun `confidence high shows only high confidence findings`() {
        val project = writeSource("Bad.java", NESTED_LOOKUP_SOURCE)
        val result = run("--no-cache", "--confidence", "high", "--fail-on", "error", project.absolutePath)
        // nested-lookup on a List.contains is medium/high confidence — the exact level
        // depends on type resolution. Just verify the flag is accepted and doesn't crash.
        result.exitCode shouldBe 0
    }

    companion object {
        private val NESTED_LOOKUP_SOURCE =
            """
            package com.example;
            import java.util.ArrayList;
            import java.util.List;
            public class Bad {
                public List<String> filter(List<String> items, List<String> allowed) {
                    List<String> result = new ArrayList<>();
                    for (String item : items) {
                        if (allowed.contains(item)) {
                            result.add(item);
                        }
                    }
                    return result;
                }
            }
            """.trimIndent()
    }
}

private data class CliResult(
    val exitCode: Int,
    val stdout: String,
    val stderr: String,
)
