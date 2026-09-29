pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
    }
}

rootProject.name = "ponder"

for (platform in listOf("common", "fabric", "neoforge")) {
    include(platform)
    project(":$platform").name = "ponder-$platform"

    include(":catnip-$platform")
    project(":catnip-$platform").projectDir = file("catnip/$platform")

    include(":testmod-$platform")
    project(":testmod-$platform").projectDir = file("testmod/$platform")
}

includeBuild("build-logic")
