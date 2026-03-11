plugins {
    id("algorilla.kotlin-library")
    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version "1.3.0"
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(project(":core"))
    implementation(project(":lang-java"))
    implementation(project(":lang-groovy"))
    implementation(project(":lang-kotlin"))
    implementation(project(":lang-javascript"))
    implementation(project(":reporting"))
    implementation(libs.kotlinx.serialization.json)
}

gradlePlugin {
    website.set("https://github.com/tvinke/algorilla")
    vcsUrl.set("https://github.com/tvinke/algorilla")
    plugins {
        create("algorilla") {
            id = "com.github.tvinke.algorilla"
            implementationClass = "com.github.tvinke.algorilla.gradle.AlgorillaPlugin"
            displayName = "Algorilla"
            description = "Detects algorithmic complexity anti-patterns in source code"
            tags.set(listOf("static-analysis", "performance", "complexity", "linter"))
        }
    }
}

// Functional test source set
val functionalTest: SourceSet by sourceSets.creating {
    compileClasspath += sourceSets.main.get().output
    runtimeClasspath += sourceSets.main.get().output
}

configurations[functionalTest.implementationConfigurationName]
    .extendsFrom(configurations.testImplementation.get())
configurations[functionalTest.runtimeOnlyConfigurationName]
    .extendsFrom(configurations.testRuntimeOnly.get())

val functionalTestTask =
    tasks.register<Test>("functionalTest") {
        testClassesDirs = functionalTest.output.classesDirs
        classpath = functionalTest.runtimeClasspath
        useJUnitPlatform()
    }

tasks.check {
    dependsOn(functionalTestTask)
}
