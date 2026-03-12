plugins {
    id("algorilla.kotlin-library")
}

dependencies {
    api(project(":core"))
    implementation(project(":lang-java"))
    implementation(libs.antlr.runtime)
    implementation(libs.treesitter)
    implementation(libs.treesitter.kotlin)
}
