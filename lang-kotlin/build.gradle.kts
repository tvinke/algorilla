plugins {
    id("algorilla.kotlin-library")
}

dependencies {
    api(project(":core"))
    implementation(libs.treesitter)
    implementation(libs.treesitter.kotlin)
}
