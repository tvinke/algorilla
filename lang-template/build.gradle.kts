plugins {
    id("algorilla.kotlin-library")
}

dependencies {
    api(project(":core"))

    // TODO: add the tree-sitter binding for your language.
    // Browse https://github.com/nicovank/java-tree-sitter/tree/main/languages for available grammars.
    // Example for Python:
    //   implementation(libs.treesitter)
    //   implementation(libs.treesitter.python)
    //
    // If you need a grammar that isn't in the version catalog yet, add it to
    // gradle/libs.versions.toml first (look at how treesitter-kotlin is defined).
}
