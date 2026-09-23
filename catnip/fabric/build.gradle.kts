plugins {
    alias(libs.plugins.loom)
    alias(libs.plugins.configure.platform)
}

loom {
    accessWidenerPath = file("src/main/resources/catnip.accesswidener")

    mods.register("catnip") {
        sourceSet(sourceSets.main.get())
        sourceSet(sourceSets.client.get())
    }

    runs {
        named("server") {
            runDir = "run/server"
        }

        configureEach {
            ideConfigGenerated(true)
            vmArg("-Dmixin.debug.export=true")
            vmArg("-XX:+IgnoreUnrecognizedVMOptions")
            vmArg("-XX:+AllowEnhancedClassRedefinition")
        }
    }
}

dependencies {
    minecraft(libs.minecraft)
    implementation(libs.bundles.fabric)

    api(libs.flywheel.fabric) {
        capabilities {
            requireFeature("api")
        }
    }
    runtimeOnly(libs.flywheel.fabric)
}
