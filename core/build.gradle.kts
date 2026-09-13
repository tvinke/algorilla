plugins {
    id("algorilla.kotlin-library")
    alias(libs.plugins.kotlin.serialization)
    id("info.solidsoft.pitest") version "1.19.0"
}

// Only present once Ted drops a licence file at the repo root - see https://www.arcmutate.com.
// Without it, PIT just runs plain (no Kotlin-inline-generics improvement, no licence error either).
val arcmutateLicence = rootProject.file("arcmutate-licence.txt")

dependencies {
    api(libs.kotlin.logging)
    implementation(libs.logback)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.antlr.runtime)
    if (arcmutateLicence.exists()) {
        pitest("com.arcmutate:pitest-kotlin-plugin:1.5.1")
        pitest("com.arcmutate:base:1.7.2")
    }
}

// Scoped mutation testing for the cc #64/#71 name-vs-type recursion fix: only the
// classification methods that fix touched, not the whole module — a whole-module PIT
// run is both slow and not interpretable as a single score (see cc plan-unit-vs-systeem.md).
pitest {
    pitestVersion.set("1.30.0")
    junit5PluginVersion.set("1.2.1")
    // STRONGER: PIT's own extended mutator set. EXTENDED: arcmutate's Java/reactive/stream
    // mutators (relevant for IOInLoopRule's monadic-target detection). EXTREME: coarse
    // "replace the whole method body" mutants - cheap here because arcmutate:base's
    // subsumption analysis (on automatically once the base plugin is present, no separate
    // config) collapses the redundant ones against the finer-grained mutators.
    mutators.set(setOf("STRONGER", "EXTENDED", "EXTREME"))
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
