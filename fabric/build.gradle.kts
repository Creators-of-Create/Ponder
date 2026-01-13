plugins {
    id("fabric-loom")
}

loom {
    accessWidenerPath = project(":common").file("src/main/resources/META-INF/${"mod_id"()}.accesswidener")

    mixin.defaultRefmapName.set("${"mod_id"()}.refmap.json")

    runs {
        configureEach {
            vmArg("-XX:+AllowEnhancedClassRedefinition")
            vmArg("-XX:+IgnoreUnrecognizedVMOptions")
            vmArg("-Dmixin.debug.export=true")
            vmArg("-Dmixin.env.remapRefMap=true")
            vmArg("-Dmixin.env.refMapRemappingFile=${projectDir}/build/createSrgToMcp/output.srg")
        }

        getByName("client") {
            client()
            ideConfigGenerated(true)
        }

        getByName("server") {
            server()
            ideConfigGenerated(true)
            runDir("run/server")
        }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${"minecraft_version"()}")
    mappings(loom.layered {
        officialMojangMappings { nameSyntheticMembers = false }
        if ("parchment_version"() != "none") {
            parchment("org.parchmentmc.data:parchment-${"parchment_minecraft_version"()}:${"parchment_version"()}@zip")
        }
    })

    modImplementation("net.fabricmc:fabric-loader:${"fabric_loader_version"()}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${"fabric_version"()}")

    modApi("dev.engine-room.flywheel:flywheel-fabric-api-${"flywheel_minecraft_version"()}:${"flywheel_version"()}")
    modImplementation("dev.engine-room.flywheel:flywheel-fabric-${"flywheel_minecraft_version"()}:${"flywheel_version"()}")

    modApi(include("fuzs.forgeconfigapiport:forgeconfigapiport-fabric:${"forgeconfigapiport_version"()}")!!)
}

tasks.named("sourcesJar") {
    mustRunAfter(project(":common").tasks.named("generatePackageInfos"))
}

operator fun String.invoke(): String {
    return rootProject.ext[this] as? String
        ?: throw IllegalStateException("Property $this is not defined")
}
