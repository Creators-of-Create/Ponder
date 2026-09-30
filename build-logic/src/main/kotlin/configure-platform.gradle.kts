// convention plugin to apply to platform subprojects.

plugins {
    `java-library`
    `maven-publish`
}

// this has to be separate for some reason
plugins.apply("net.createmod.ponder.gradle")
plugins.apply("setup-git-hash")

// set coordinates based on parent project, ex. net.createmod.ponder:ponder-fabric
val modName: String = project.name.substringBeforeLast("-")
val platform: String = project.name.substringAfterLast("-")
group = "net.createmod.$modName"

// keep version synchronized with the root project
version = rootProject.version

repositories {
    maven("https://maven.createmod.net") // Flywheel
    maven("https://raw.githubusercontent.com/Fuzss/modresources/main/maven/") // Forge Config API Port
}

java {
    withSourcesJar()
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

tasks.withType<Jar> {
    // copy the license file into every built jar
    from(rootProject.file("LICENSE"))
}

val libs: VersionCatalog = versionCatalogs.named("libs")
fun versionOf(name: String): String {
    val version = libs.findVersion(name).get().toString()

    // normalize minecraft versions to semver on fabric because they still do that for some reason
    if (name == "minecraft" && project.name == "fabric")
        return version
            .replace("snapshot-", "alpha.")
            .replace("pre-", "pre.")
            .replace("rc-", "rc.")

    return version
}

val authors = findProperty("authors") as String
val contributors = findProperty("contributors") as String

// expand placeholders in metadata files
tasks.processResources {
    val properties = mapOf(
        "version" to project.version,
        "group" to project.group,
        "minecraft_version" to versionOf("minecraft"),
        "neo_version" to versionOf("neoforge"),
        "fabric_api_version" to versionOf("fabric-api"),
        "fabric_loader_version" to versionOf("fabric-loader"),
        "authors" to authors,
        "contributors" to contributors,
        "authors_json" to formatForJson(authors),
        "contributors_json" to formatForJson(contributors)
    )

    inputs.properties(properties)

    filesMatching(setOf("fabric.mod.json", "META-INF/neoforge.mods.toml")) {
        expand(properties)
    }
}

// don't publish the testmod
if (parent!!.name != "testmod") {
    publishing {
        publications.create<MavenPublication>("mavenJava") {
            from(components["java"])
        }

        repositories {
            val mavenUsername = providers.environmentVariable("MAVEN_USERNAME")
            val mavenPassword = providers.environmentVariable("MAVEN_PASSWORD")
            if (mavenUsername.isPresent && mavenPassword.isPresent) {
                maven {
                    url = uri("https://maven.createmod.net")
                    credentials {
                        username = mavenUsername.get()
                        password = mavenPassword.get()
                    }
                }
            }
        }
    }
}

// will only exist in common/fabric
val loom: Any? = extensions.findByName("loom")
// reflection in the buildscript. have I hit a new low?
// we need to call this now or else the sourceSet won't exist, and
// I don't even know where to begin with compiling against loom here.
loom?.javaClass?.getMethod("splitEnvironmentSourceSets")?.run {
    invoke(loom)
    plugins.apply("register-client-jar")
}

// generate package-infos for the main (and client, if present) sourceSet(s)
//extensions.getByType<PackageInfosExtension>().sources(sourceSets.named { it == "main" || it == "client" })

when (platform) {
    "common" -> plugins.apply("provide-common")
    "fabric" -> plugins.apply("consume-common-split")
    else -> plugins.apply("consume-common-merged")
}

// trick to sneak multiple entries into a single placeholder in a JSON file.
// the file must be valid even with placeholders, so we can't just do something like this: [${placeholder}]
// instead, the placeholder is expected to be in a string, like this: ["${placeholder}"]
// this takes a string in the format 'a, b, c' and adds quotes, so the end result will be like this: a", "b", "c
// when filled into the placeholder, you get a valid list: ["a", "b", "c"]
fun formatForJson(entries: String): String {
    return entries.split(", ").joinToString(separator = "\", \"")
}
