plugins {
    id("algorilla.kotlin-application")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.shadow)
}

application {
    mainClass.set("com.github.tvinke.algorilla.cli.AlgorillaCommandKt")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":lang-java"))
    implementation(project(":lang-groovy"))
    implementation(project(":lang-kotlin"))
    implementation(project(":lang-javascript"))
    implementation(project(":reporting"))
    implementation(libs.picocli)
    implementation(libs.kaml)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.logback)
    implementation(libs.kotlin.scripting.jsr223)
    annotationProcessor(libs.picocli.codegen)
}

tasks.shadowJar {
    archiveBaseName.set("algorilla")
    archiveClassifier.set("")
    mergeServiceFiles()
}

val generateVersionFile by tasks.registering {
    val outputDir = layout.buildDirectory.dir("generated/version")
    val projectVersion = project.version.toString()
    outputs.dir(outputDir)
    doLast {
        val file = outputDir.get().file("algorilla-version.properties").asFile
        file.parentFile.mkdirs()
        file.writeText("version=$projectVersion\n")
    }
}

sourceSets.main {
    resources.srcDir(generateVersionFile)
}

tasks.processResources {
    dependsOn(generateVersionFile)
}
