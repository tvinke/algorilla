pluginManagement {
    includeBuild("build-logic")
}

rootProject.name = "algorilla"

include("core")
include("lang-java")
include("lang-groovy")
include("lang-kotlin")
include("lang-javascript")
include("reporting")
include("cli")
include("gradle-plugin")

// lang-template/ — contributor reference, not included in the build.
// Copy it as your starting point when adding a new language.
// See docs/docs/developer/adding-languages.md for instructions.
