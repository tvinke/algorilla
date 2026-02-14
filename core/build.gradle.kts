plugins {
    id("algorilla.kotlin-library")
}

dependencies {
    api(libs.kotlin.logging)
    implementation(libs.logback)
}
