plugins {
    id("net.neoforged.moddev")
}

neoForge {
    version = "neo_version"()

    accessTransformers.from(project(":ponder-common").file("src/main/resources/META-INF/accesstransformer.cfg"))

    parchment {
        minecraftVersion = "minecraft_version"()
        mappingsVersion = "parchment_version"()
    }

    runs {
        create("client") {
            client()
        }

        create("server") {
            server()

            gameDirectory = project.file("run/server")
        }

        configureEach {
            jvmArgument("-XX:+AllowEnhancedClassRedefinition")
            jvmArgument("-XX:+IgnoreUnrecognizedVMOptions")
            jvmArgument("-Dmixin.debug.export=true")
            jvmArgument("-Dmixin.env.remapRefMap=true")
            jvmArgument("-Dmixin.env.refMapRemappingFile=${projectDir}/build/createSrgToMcp/output.srg")
        }
    }

    mods {
        create("mod_id"()) {
            sourceSet(sourceSets["main"])
        }
    }
}

dependencies {
    compileOnly("dev.engine-room.flywheel:flywheel-neoforge-api-${"minecraft_version"()}:${"flywheel_version"()}")
    runtimeOnly("dev.engine-room.flywheel:flywheel-neoforge-${"minecraft_version"()}:${"flywheel_version"()}")
}

operator fun String.invoke(): String {
    return rootProject.ext[this] as? String
        ?: throw IllegalStateException("Property $this is not defined")
}
