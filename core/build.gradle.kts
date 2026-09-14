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
    testImplementation(libs.konsist)
    testImplementation(libs.archunit.junit5)
    if (arcmutateLicence.exists()) {
        pitest("com.arcmutate:pitest-kotlin-plugin:1.5.1")
        pitest("com.arcmutate:base:1.7.2")
    }
}

// Scoped mutation testing for the name-vs-type recursion fix: only the classification
// methods that fix touched, not the whole module - a whole-module PIT run is both slow
// and not interpretable as a single score.
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
    // Rule-interface boilerplate (id/name/languages/subsumes/requiresTypeContext/aliases) -
    // untouched by the buildChain fix, excluded so the score reflects the changed
    // classification logic (buildChain/checkFunction/buildFinding), not unrelated getters.
    excludedMethods.set(
        setOf("getId", "getName", "getAliases", "getLanguages", "getSubsumes", "getRequiresTypeContext"),
    )
    outputFormats.set(setOf("HTML", "XML"))
}

// A file named *PropertyTest.kt promises a generator exploring an effectively open domain
// (Arb.string/Arb.int/Arb.list/.../checkAll), not a hand-picked Arb.of(fixedList) wrapped in
// forAll - that's a parameterized example test and should say so in its name. See
// CODING_GUIDELINES.md for the full distinction and WordBoundaryMatchingPropertyTest.kt for
// the canonical real property test.
val propertyTestNamingAllowlist =
    setOf(
        // Deliberately tests a small, curated set of NAMED real-world scenarios (Broadleaf's
        // super.getSectionKey(), Fineract's modifyLoanApprovedAmount) rather than exploring
        // an open domain - a legitimate design choice for the recursion-family fix, not an
        // oversight. Kept as *PropertyTest.kt rather than renamed because the pitest
        // targetTests block above already references it by this class name.
        "NameVsTypeRecursionPropertyTest",
    )

val realGeneratorMarkers =
    listOf(
        Regex("""Arb\.(string|int|long|double|float|boolean|char|byte|short|uuid|list|set|map|bind|choice|pair|triple)\b"""),
        Regex("""\bcheckAll\b"""),
        Regex(""":\s*Arb<"""),
    )

tasks.register("checkPropertyTestNaming") {
    group = "verification"
    description =
        "Fails if a *PropertyTest.kt file contains no real generator " +
        "(Arb.string/Arb.int/Arb.list/checkAll/a custom Arb<T> function) - " +
        "see CODING_GUIDELINES.md."
    val propertyTestFiles = fileTree("src/test/kotlin") { include("**/*PropertyTest.kt") }
    inputs.files(propertyTestFiles)
    doLast {
        val offenders = mutableListOf<String>()
        propertyTestFiles.forEach { file ->
            val className = file.name.removeSuffix(".kt")
            if (className in propertyTestNamingAllowlist) return@forEach
            val text = file.readText()
            if (realGeneratorMarkers.none { it.containsMatchIn(text) }) {
                offenders += file.relativeTo(projectDir).path
            }
        }
        if (offenders.isNotEmpty()) {
            throw GradleException(
                "These *PropertyTest.kt files have no real generator - they're parameterized " +
                    "example tests (Arb.of(fixedList) or plain examples), not property tests. " +
                    "Rename to *Test.kt (drop \"Property\"), or add a reasoned entry to " +
                    "propertyTestNamingAllowlist in core/build.gradle.kts if the fixed scenario " +
                    "set is deliberate:\n" +
                    offenders.joinToString("\n") { "  - $it" },
            )
        }
    }
}

tasks.named("check") {
    dependsOn("checkPropertyTestNaming")
}
