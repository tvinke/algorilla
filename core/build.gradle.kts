plugins {
    id("algorilla.kotlin-library")
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    api(libs.kotlin.logging)
    implementation(libs.logback)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.antlr.runtime)
}
