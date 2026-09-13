plugins {
    id("algorilla.kotlin-library")
    alias(libs.plugins.kotlin.serialization)
    id("info.solidsoft.pitest") version "1.15.0"
}

dependencies {
    api(libs.kotlin.logging)
    implementation(libs.logback)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.antlr.runtime)
}

// Scoped mutation testing for the cc #64/#71 name-vs-type recursion fix: only the
// classification methods that fix touched, not the whole module — a whole-module PIT
// run is both slow and not interpretable as a single score (see cc plan-unit-vs-systeem.md).
pitest {
    pitestVersion.set("1.17.4")
    junit5PluginVersion.set("1.2.1")
    targetClasses.set(
        setOf(
            "com.github.tvinke.algorilla.util.RecursionDetectorKt",
            "com.github.tvinke.algorilla.rules.builtin.ChainedGettersRule",
        ),
    )
    targetTests.set(
        setOf(
            "com.github.tvinke.algorilla.util.NameVsTypeRecursionPropertyTest",
            "com.github.tvinke.algorilla.util.RecursionDetectorTest",
            "com.github.tvinke.algorilla.rules.builtin.ChainedGettersRuleTest",
        ),
    )
    // Rule-interface boilerplate (id/name/languages/subsumes/requiresTypeContext/aliases) —
    // untouched by the cc #64 buildChain fix, excluded so the score reflects the changed
    // classification logic (buildChain/checkFunction/buildFinding), not unrelated getters.
    excludedMethods.set(
        setOf("getId", "getName", "getAliases", "getLanguages", "getSubsumes", "getRequiresTypeContext"),
    )
    outputFormats.set(setOf("HTML", "XML"))
}
