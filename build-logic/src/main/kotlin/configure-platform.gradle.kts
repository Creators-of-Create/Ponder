import net.createmod.pondergradle.nullability.PackageInfosExtension
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.credentials.PasswordCredentials
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.plugins.BasePluginExtension
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.tasks.Jar
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.language.jvm.tasks.ProcessResources
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.withType

// convention plugin to apply to platform subprojects.

plugins {
    `java-library`
    `maven-publish`
}

// this has to be separate for some reason
plugins.apply("net.createmod.ponder.gradle")
plugins.apply("setup-git-hash")

// set up name and group based on parent project, ex. net.createmod.ponder:ponder-fabric
val modName: String = parent!!.name
val archiveName = "$modName-$name"
extensions.configure<BasePluginExtension>("base") {
    archivesName.set(archiveName)
}
group = "net.createmod.$modName"

// keep version synchronized with the root project
version = rootProject.version

repositories {
    if (providers.gradleProperty("ponder.useMavenLocal").isPresent) {
        mavenLocal()
    }
    maven("https://maven.neoforged.net/releases") // NeoForge API
    maven("https://maven.createmod.net") // Flywheel
    maven("https://raw.githubusercontent.com/Fuzss/modresources/main/maven/") // Forge Config API Port
}

extensions.configure<JavaPluginExtension>("java") {
    withSourcesJar()
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    // copy the license file into every built jar
    from(rootProject.file("LICENSE"))
    exclude("net/createmod/catnip/net/base/package-info.java")
}

val libs: VersionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")
fun versionOf(name: String): String {
    val version = libs.findVersion(name).get().toString()

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
tasks.named<ProcessResources>("processResources") {
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
    extensions.configure<PublishingExtension>("publishing") {
        publications.create<MavenPublication>("mavenJava") {
            artifactId = archiveName
            from(components.getByName("java"))
        }

        repositories {
            maven("https://maven.createmod.net") {
                name = "create"
                credentials(PasswordCredentials::class)
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

val sourceSets = extensions.getByType<SourceSetContainer>()

// generate package-infos for the main (and client, if present) sourceSet(s)
extensions.getByType<PackageInfosExtension>().sources(sourceSets.named { it == "main" || it == "client" })

if (name != "common") {
    tasks.withType<Jar> {
        dependsOn(project(":common").tasks.named("generatePackageInfos"))
        dependsOn(project(":common").tasks.matching { it.name == "generateClientPackageInfos" })
        if (parent?.name == "catnip") {
            dependsOn(project(":catnip:common").tasks.named("generatePackageInfos"))
            dependsOn(project(":catnip:common").tasks.matching { it.name == "generateClientPackageInfos" })
        }
    }

    tasks.withType<JavaCompile> {
        dependsOn(project(":common").tasks.named("generatePackageInfos"))
        dependsOn(project(":common").tasks.matching { it.name == "generateClientPackageInfos" })
        dependsOn(project(":common").tasks.matching { it.name == "compileClientJava" })
        dependsOn(project(":common").tasks.matching { it.name == "processClientResources" })
        if (parent?.name == "catnip") {
            dependsOn(project(":catnip:common").tasks.named("generatePackageInfos"))
            dependsOn(project(":catnip:common").tasks.matching { it.name == "generateClientPackageInfos" })
            dependsOn(project(":catnip:common").tasks.matching { it.name == "compileClientJava" })
            dependsOn(project(":catnip:common").tasks.matching { it.name == "processClientResources" })
        }
    }
}

when (name) {
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
