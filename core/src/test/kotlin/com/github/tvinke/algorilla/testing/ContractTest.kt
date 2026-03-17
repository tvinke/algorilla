package com.github.tvinke.algorilla.testing

import org.junit.jupiter.api.Tag

/**
 * Marks a test as a **contract test** — it verifies a public API surface that
 * external consumers depend on. Breaking a contract test means something visible
 * to users has changed: CLI flags, JSON schema fields, SARIF structure, Gradle
 * plugin properties, GitHub Action inputs, or exit code semantics.
 *
 * Before changing code that causes a contract test to fail:
 *
 * 1. Check which distributions are affected (CLI, Gradle plugin, GitHub Action, npm, Docker)
 * 2. Update all affected distribution configs (action.yml, AlgorillaExtension.kt, docs)
 * 3. Consider whether the change needs a deprecation period (after 1.0)
 *
 * Contract tests can be run in isolation with:
 * ```
 * ./gradlew test -Dinclude.tags=contract
 * ```
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Tag("contract")
public annotation class ContractTest
