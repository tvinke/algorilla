plugins {
    id("algorilla.kotlin-library")
}

dependencies {
    api(project(":core"))
    implementation(libs.antlr.runtime)
    implementation(libs.treesitter)
    implementation(libs.treesitter.javascript)
    implementation(libs.treesitter.typescript)
}
