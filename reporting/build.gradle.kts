plugins {
    id("algorilla.kotlin-library")
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    api(project(":core"))
    implementation(libs.sarif4k)
    implementation(libs.kotlinx.serialization.json)
}
