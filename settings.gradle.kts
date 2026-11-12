pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://repo.spongepowered.org/repository/maven-public/")
        maven("https://maven.parchmentmc.org")
        maven("https://maven.neoforged.net/releases")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

val modId = providers.gradleProperty("mod_id").get()

rootProject.name = modId

rootDir.listFiles { file ->
    file.isDirectory
        && file.name != "buildSrc"
        && (file.resolve("build.gradle").exists() || file.resolve("build.gradle.kts").exists())
}.forEach {
    val projectName = ":${modId}-${it.toRelativeString(rootDir)}"

    include(projectName)
    project(projectName).projectDir = it
}
