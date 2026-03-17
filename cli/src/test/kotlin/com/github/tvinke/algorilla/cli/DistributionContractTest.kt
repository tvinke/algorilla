package com.github.tvinke.algorilla.cli

import io.kotest.matchers.collections.shouldContainAll
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import picocli.CommandLine
import java.io.File

/**
 * Contract tests that verify the CLI option surface matches what other distribution
 * methods (GitHub Action, Gradle plugin) expose. If a CLI flag is renamed or removed,
 * these tests fail — signaling that the other distributions need updating too.
 *
 * This prevents drift between the CLI, action.yml, and Gradle plugin extension.
 */
@Tag("contract")
internal class DistributionContractTest {
    private val spec = CommandLine(AlgorillaCommand()).commandSpec

    private fun optionNames(): Set<String> = spec.options().flatMap { it.names().toList() }.toSet()

    // -- CLI option existence (other distributions depend on these) --

    @Test
    fun `CLI exposes all flags that action_yml maps to`() {
        // action.yml inputs map to these CLI flags.
        // If any of these are renamed, action.yml's "Run algorilla" step breaks.
        val requiredByAction =
            listOf(
                "--format",
                "--severity",
                "--fail-on",
                "--rule",
                "--baseline",
                "--language",
                "--output",
            )
        optionNames() shouldContainAll requiredByAction
    }

    @Test
    fun `CLI exposes all flags that Gradle plugin maps to`() {
        // AlgorillaTask passes these to the engine. If CLI semantics change,
        // the Gradle plugin's behavior should change to match.
        val requiredByGradle =
            listOf(
                "--format",
                "--severity",
                "--fail-on",
                "--rule",
                "--baseline",
                "--exclude",
                "--include-tests",
                "--output",
            )
        optionNames() shouldContainAll requiredByGradle
    }

    @Test
    fun `CLI exposes all flags that npm wrapper passes through`() {
        // npm/bin/algorilla.js does process.argv passthrough, so all flags
        // must remain valid. These are the ones documented in installation.md.
        val documentedFlags =
            listOf(
                "--format",
                "--severity",
                "--fail-on",
                "--rule",
                "--baseline",
                "--language",
                "--exclude",
                "--include-tests",
                "--no-cache",
                "--output",
                "--confidence",
                "--color",
                "--list-rules",
                "--accept",
                "--verbose",
            )
        optionNames() shouldContainAll documentedFlags
    }

    // -- action.yml contract: verify the input-to-flag mapping is correct --

    @Test
    fun `action_yml inputs match documented CLI flag names`() {
        // action.yml constructs args like: --format ${INPUT_FORMAT} --severity ${INPUT_SEVERITY}
        // This test reads action.yml and verifies every --flag it references exists in the CLI.
        val actionYml = File("action.yml")
        if (!actionYml.exists()) return // skip when running from a different working dir

        val flagPattern = Regex("""--([a-z][-a-z]*)""")
        val flagsInAction =
            flagPattern
                .findAll(actionYml.readText())
                .map { "--${it.groupValues[1]}" }
                .toSet()

        val cliFlags = optionNames()
        for (flag in flagsInAction) {
            cliFlags shouldContainAll listOf(flag)
        }
    }
}
