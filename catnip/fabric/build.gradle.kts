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
            runDirectory = file("run/server")
        }

        configureEach {
            generateRunConfig = true
            jvmArguments.add("-Dmixin.debug.export=true")
            jvmArguments.add("-XX:+IgnoreUnrecognizedVMOptions")
            jvmArguments.add("-XX:+AllowEnhancedClassRedefinition")
        }
    }
}

dependencies {
    minecraft(libs.minecraft)
    implementation(libs.bundles.fabric)

    compileOnly(libs.flywheel.fabric) {
        capabilities {
            requireFeature("api")
        }
    }
    runtimeOnly(libs.flywheel.fabric)
}
