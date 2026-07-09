pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

rootProject.name = "ponder"

val platforms = providers.gradleProperty("ponder.platforms")
    .orNull
    ?.split(",")
    ?.map(String::trim)
    ?.filter(String::isNotEmpty)
    ?: listOf("common", "fabric", "neoforge")

for (platform in platforms) {
    include(platform)

    include(":catnip:$platform")
    include(":testmod:$platform")
}

includeBuild("build-logic")
