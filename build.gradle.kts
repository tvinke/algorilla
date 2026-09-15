import de.aaschmid.gradle.plugins.cpd.Cpd
import org.gradle.api.file.SourceDirectorySet

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.shadow) apply false
    `lifecycle-base`
    id("de.aaschmid.cpd") version "3.5"
}

repositories {
    mavenCentral()
}

cpd {
    toolVersion = "7.27.0"
    // ignoreFailures below only suppresses CPD's "duplicates found" path - a tokenizer/
    // analysis error (PMD's Kotlin support is younger than its Java support) is a
    // separate failOnError gate that defaults to true and would hard-fail the build
    // regardless. Off, to actually match the "report-only, don't break the build" intent.
    setFailOnError(false)
}

// One task for all hand-written Kotlin source, gathered from every subproject listed in
// settings.gradle.kts (core, lang-*, reporting, cli, gradle-plugin) - not build-logic,
// which is a separate included build (pluginManagement { includeBuild(...) }) and never
// shows up in rootProject.subprojects, so its own .gradle.kts convention plugins go
// unscanned here. core/src/main/java (ANTLR-generated JavaLexer/JavaParser/...) is
// excluded by the "**/*.kt" filter on the source below - duplication in generated code
// wouldn't be actionable anyway. There's currently no hand-written Java anywhere in the
// build (that's the whole reason no cpdJava task exists), so this task is the only CPD
// coverage the project has; if a module ever grows real hand-written Java, that'll need
// its own task rather than silently going unscanned. lang-groovy/lang-javascript are
// Kotlin implementations of those languages' parsers, not real Groovy/JS, so they don't
// need a task of their own either - this one task covers them along with everyone else.
val cpdKotlin by tasks.registering(Cpd::class) {
    description = "Runs PMD CPD (copy-paste detection) across all main Kotlin sources."
    language = "kotlin"
    minimumTokenCount = 75
    // #132/#134 (ANTLR visitor scaffolding, tree-sitter visitors) are closed, but a fresh
    // run still turns up 17 duplications: the #133 loop-scan cluster is still open (plus a
    // few newer rule classes with the same shape that #133 predates), and GroovyIRVisitor/
    // JavaLanguageParser still share several visitor overrides #132's fix didn't reach. No
    // freeze/baseline mechanism here like ArchUnit's FreezingArchRule, so report-only for
    // now rather than failing every build on debt that isn't cleared yet - flip this back
    // to false once #133 (and its now-larger sibling cluster) is closed out.
    ignoreFailures = true
}

// Root-level, not a build-logic convention plugin: unlike detekt/ktlint (one task per
// module, so each module's own convention-plugin application is enough), CPD needs a
// single task that sees every module's Kotlin source at once to catch cross-module
// duplication - splitting it per-module would blind it to exactly the GroovyIRVisitor/
// JavaLanguageParser-style duplication (lang-groovy vs lang-java) this task exists to catch.
subprojects {
    plugins.withType<JavaBasePlugin> {
        // Captured here (Project scope) rather than inline inside cpdKotlin.configure{} -
        // Task also has an .extensions property, so resolving it from inside the task's own
        // configure block would silently shadow this and reach the task's (empty) extension
        // container instead of the subproject's.
        val sourceSets = extensions.getByType<JavaPluginExtension>().sourceSets
        cpdKotlin.configure {
            // sourceSet.allJava only picks up .java files even when a kotlin srcDir has
            // been registered on it - go straight to the "kotlin" SourceDirectorySet the
            // Kotlin Gradle plugin adds to each source set instead. That SourceDirectorySet's
            // srcDirs still include src/main/java though (Kotlin/JVM registers it there for
            // joint compilation), so `.matching { include("**/*.kt") }` does the actual
            // filtering here rather than leaving it to CPD's own per-language file
            // discovery - relying on that would've quietly tokenized the ANTLR-generated
            // core/src/main/java/**/*.java as Kotlin the moment that discovery behavior
            // changed under us.
            source(
                sourceSets.named("main").map {
                    (it.extensions.getByName("kotlin") as SourceDirectorySet).matching {
                        include("**/*.kt")
                    }
                },
            )
        }
    }
}

// The plugin's own default task, wired into check automatically once lifecycle-base
// is present - not what we want (language defaults to java, and it'd sweep up the
// ANTLR-generated core/src/main/java sources along with it). cpdKotlin replaces it.
tasks.named<Cpd>("cpdCheck") {
    enabled = false
}

tasks.check {
    dependsOn(cpdKotlin)
}
