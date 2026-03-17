package com.github.tvinke.algorilla.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension

/**
 * Gradle plugin that registers the `algorilla` extension and an `algorilla` task
 * which runs analysis on the project's source files.
 */
public class AlgorillaPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension =
            project.extensions.create("algorilla", AlgorillaExtension::class.java).apply {
                minSeverity.convention("warning")
                failOn.convention("info")
                format.convention("json")
                outputFile.convention(
                    project.layout.buildDirectory
                        .file("reports/algorilla.json")
                        .map { it.asFile },
                )
                excludePatterns.convention(emptyList())
                rules.convention(emptyList())
                includeTests.convention(false)
                limit.convention(0)
            }

        project.tasks.register("algorilla", AlgorillaTask::class.java) { task ->
            configureTask(task, extension, project)
        }
    }

    private fun configureTask(
        task: AlgorillaTask,
        extension: AlgorillaExtension,
        project: Project,
    ) {
        task.group = "verification"
        task.description = "Detect algorithmic complexity anti-patterns"

        task.minSeverity.set(extension.minSeverity)
        task.failOn.set(extension.failOn)
        task.format.set(extension.format)
        task.outputFile.set(extension.outputFile)
        task.excludePatterns.set(extension.excludePatterns)
        task.ruleIds.set(extension.rules)
        task.includeTests.set(extension.includeTests)
        task.baseline.set(extension.baseline)
        task.limit.set(extension.limit)
        task.sourceDirectories.setFrom(resolveSourceDirs(project, extension))
    }

    private fun resolveSourceDirs(
        project: Project,
        extension: AlgorillaExtension,
    ): List<java.io.File> {
        val java = project.extensions.findByType(JavaPluginExtension::class.java)
        if (java != null) {
            val sets =
                if (extension.includeTests.getOrElse(false)) {
                    java.sourceSets
                } else {
                    java.sourceSets.matching { it.name == "main" }
                }
            return sets.flatMap { ss -> ss.allSource.srcDirs }
        }
        return listOf(
            project.file("src/main/java"),
            project.file("src/main/kotlin"),
            project.file("src/main/groovy"),
        ).filter { it.isDirectory }
    }
}
