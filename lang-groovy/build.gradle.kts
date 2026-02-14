plugins {
    id("algorilla.kotlin-library")
}

dependencies {
    api(project(":core"))
    implementation(libs.antlr.runtime)
}
