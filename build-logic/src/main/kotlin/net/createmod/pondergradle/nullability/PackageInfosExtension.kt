package net.createmod.pondergradle.nullability

import org.gradle.api.Project
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.SourceSet
import org.gradle.kotlin.dsl.register

// Copied from https://github.com/Engine-Room/Flywheel/blob/1.20.1/dev/buildSrc/src/main/kotlin/dev/engine_room/gradle/nullability/GeneratePackageInfosTask.kt
open class PackageInfosExtension(private val project: Project) {
    fun sources(vararg sourceSets: SourceSet) {
        for (sourceSet in sourceSets) {
            forSourceSet(sourceSet)
        }
    }

    fun sources(sourceSets: Iterable<SourceSet>) {
        for (sourceSet in sourceSets) {
            forSourceSet(sourceSet)
        }
    }

    private fun forSourceSet(sourceSet: SourceSet) {
        // We have to capture the source set name for the lazy string literals,
        // otherwise it'll just be whatever the last source set is in the list.
        val sourceSetName = sourceSet.name
        val output = project.layout.buildDirectory.dir("generatedPackageInfos/$sourceSetName")
        val taskName = sourceSet.getTaskName("generate", "PackageInfos")
        val task = project.tasks.register<GeneratePackageInfosTask>(taskName) {
            group = "ponder"
            description = "Generates package-info files for $sourceSetName packages."

            // Only apply to default source directory since we also add the generated
            // sources to the source set.
            sourceRoot.set(project.file("src/$sourceSetName/java"))
            outputDir.set(output)
        }

        sourceSet.java.srcDir(task)

        project.tasks.matching { it.name == "ideaSyncTask" || it.name == "neoForgeIdeSync" }.configureEach {
            finalizedBy(task)
        }

        val cleanTask = project.tasks.register<Delete>(sourceSet.getTaskName("clean", "PackageInfos")) {
            group = "ponder"
            delete(output)
        }

        project.tasks.named("clean").configure {
            dependsOn(cleanTask)
        }
    }
}
