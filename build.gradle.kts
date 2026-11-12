import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.gradle.internal.extensions.stdlib.capitalized
import java.net.URI

plugins {
    java
    `maven-publish`
    id("fabric-loom") apply false
    id("net.neoforged.moddev") apply false
}

apply(from = "gradle/property_loader.gradle.kts")

val buildNumber = providers.environmentVariable("BUILD_NUMBER").orNull
val gitHash = calculateGitHash() + (if (hasUnstaged()) "-modified" else "")

allprojects {
    apply(plugin = "java")

    group = "maven_group"()
    version = "${"mod_version"()}.${buildNumber ?: "0"}+mc${"minecraft_version"()}"
}

subprojects {
    apply(plugin = "maven-publish")
    apply(plugin = "net.createmod.ponder.gradle")

    apply(from = "../gradle/property_loader.gradle.kts")
    apply(from = "../gradle/signing.gradle.kts")

    val capitalizedName = project.name.capitalized()

    repositories {
        exclusiveMaven("https://repo.spongepowered.org/repository/maven-public/", "org.spongepowered:mixin")
        exclusiveMaven("https://maven.createmod.net", "dev.engine-room.flywheel")
        exclusiveMaven(
            "https://raw.githubusercontent.com/Fuzss/modresources/main/maven/",
            "net.minecraftforge:forgeconfigapiport-fabric",
            "fuzs.forgeconfigapiport:forgeconfigapiport-fabric",
            "fuzs.forgeconfigapiport:forgeconfigapiport-common-neoforgeapi"
        )
        exclusiveMaven("https://mvn.devos.one/releases/", "io.github.fabricators_of_create.Porting-Lib")
        exclusiveMaven(
            "https://maven.jamieswhiteshirt.com/libs-release",
            "com.jamieswhiteshirt:reach-entity-attributes"
        )
        maven("https://maven.neoforged.net/releases")
        flatDir {
            dirs(rootProject.file("libs"))
        }
    }

    java {
        toolchain.languageVersion = JavaLanguageVersion.of(21)
        withSourcesJar()
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
    }

    tasks.withType<Jar> {
        from(rootProject.file("LICENSE")) {
            rename { "${it}_${"mod_name"()}" }
        }
    }

    tasks.jar {
        manifest.attributes(
            mapOf(
                "Specification-Title" to "mod_name"(),
                "Specification-Vendor" to "mod_author"(),
                "Specification-Version" to project.version,
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version,
                "Implementation-Vendor" to "mod_author"(),
                "Built-On-Minecraft" to "minecraft_version"(),
                "Git-Hash" to "\"$gitHash\"",
            )
        )
    }

    tasks.processResources {
        val expandProps = mapOf(
            "version" to project.version,
            "group" to project.group, //Else we target the task's group.
            "minecraft_version" to "minecraft_version"(),
            "neo_version" to "neo_version"(),
            "fabric_version" to "fabric_version"(),
            "fabric_loader_version" to "fabric_loader_version"(),
            "flywheel_version_range" to "flywheel_version_range"(),
            "mod_name" to "mod_name"(),
            "mod_author" to "mod_author"(),
            "mod_credit" to "mod_credit"(),
            "mod_id" to "mod_id"(),
            "mod_homepage" to "mod_homepage"(),
            "mod_source" to "mod_source"(),
            "mod_issues" to "mod_issues"(),
            "mod_description" to "mod_description"(),
            "mod_license" to "mod_license"()
        )

        filesMatching(setOf("pack.mcmeta", "fabric.mod.json", "META-INF/neoforge.mods.toml", "*.mixins.json")) {
            expand(expandProps)
        }
        inputs.properties(expandProps)

        doLast {
            fileTree(outputs.files.asPath) {
                include("**/*.json", "**/*.mcmeta")
            }.forEach { file ->
                file.writeText(JsonOutput.toJson(JsonSlurper().parse(file)))
            }
        }
    }

    val newMavenCords = "${project.group}:${project.name}:${project.version}"
    val oldMavenCords = fun(): String {
        val oldLoaderName = when (val name = project.name.split("-")[1]) {
            "neoforge" -> "NeoForge"
            else -> name.replaceFirstChar { it.uppercase() }
        }
        val oldArtifactId = "${"mod_name"()}-$oldLoaderName-${"minecraft_version"()}"
        val oldVersion = "${"mod_version"()}.${buildNumber ?: "0"}"
        return "${project.group}:$oldArtifactId:$oldVersion"
    }()

    configurations.configureEach {
        outgoing {
            capability(newMavenCords)
            capability(oldMavenCords)
        }
    }

    publishing {
        publications.create<MavenPublication>("maven${capitalizedName}") {
            from(components["java"])
        }

        repositories {
            if (project.hasProperty("mavenUsername") && project.hasProperty("mavenPassword") && project.hasProperty("mavenURL")) {
                project.logger.lifecycle("Adding maven from secrets")

                maven {
                    credentials {
                        username = project.property("mavenUsername").toString()
                        password = project.property("mavenPassword").toString()
                    }
                    url = URI.create(project.property("mavenURL").toString())
                }
            }
        }
    }

    // from here down is platform configuration
    if (project.path == ":ponder-common") {
        return@subprojects
    }

    configurations {
        create("commonJava") {
            isCanBeResolved = true
        }
        create("commonResources") {
            isCanBeResolved = true
        }
    }

    dependencies {
        compileOnly(project(":ponder-common"))
        "commonJava"(project(path = ":ponder-common", configuration = "commonJava"))
        "commonResources"(project(path = ":ponder-common", configuration = "commonResources"))
    }

    tasks.named<JavaCompile>("compileJava") {
        dependsOn(configurations["commonJava"])
        source(configurations["commonJava"])
    }

    tasks.processResources {
        dependsOn(configurations["commonResources"])
        from(configurations["commonResources"])
    }

    tasks.named<Jar>("sourcesJar") {
        dependsOn(configurations["commonJava"])
        from(configurations["commonJava"])
        dependsOn(configurations["commonResources"])
        from(configurations["commonResources"])
    }
}

fun calculateGitHash(): String {
    try {
        val output = providers.exec {
            commandLine("git", "rev-parse", "HEAD")
        }
        return output.standardOutput.asText.get().trim()
    } catch (_: Throwable) {
        return "unknown"
    }
}

fun hasUnstaged(): Boolean {
    try {
        val output = providers.exec {
            commandLine("git", "status", "--porcelain")
        }
        val result = output.standardOutput.asText.get().replace("/M gradlew(\\.bat)?/", "").trim()
        if (!result.isEmpty())
            println("Found stageable results:\n ${result}\n")
        return !result.isEmpty()
    } catch (_: Throwable) {
        return false
    }
}

fun RepositoryHandler.exclusiveMaven(url: String, vararg coords: String) {
    exclusiveContent {
        forRepository { maven(url) }
        filter {
            coords.forEach { coordinate ->
                if (":" in coordinate) {
                    val (group, module) = coordinate.split(":", limit = 2)
                    includeModule(group, module)
                } else {
                    includeGroup(coordinate)
                }
            }
        }
    }
}

operator fun String.invoke(): String {
    return rootProject.ext[this] as? String
        ?: throw IllegalStateException("Property $this is not defined")
}
