plugins {
    id("fabric-loom")
}

loom {
    accessWidenerPath = project(":ponder-common").file("src/main/resources/META-INF/${"mod_id"()}.accesswidener")

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
    mappings(loom.officialMojangMappings())

    modImplementation("net.fabricmc:fabric-loader:${"fabric_loader_version"()}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${"fabric_version"()}")

    modApi("dev.engine-room.flywheel:flywheel-fabric-api-${"minecraft_version"()}:${"flywheel_version"()}")
    modImplementation("dev.engine-room.flywheel:flywheel-fabric-${"minecraft_version"()}:${"flywheel_version"()}")

    modApi(include("fuzs.forgeconfigapiport:forgeconfigapiport-fabric:${"forgeconfigapiport_version"()}")!!)
    implementation(include("javax.annotation:javax.annotation-api:1.3.2")!!)
    implementation(include("com.google.code.findbugs:jsr305:3.0.2")!!)
}

operator fun String.invoke(): String {
    return rootProject.ext[this] as? String
        ?: throw IllegalStateException("Property $this is not defined")
}
