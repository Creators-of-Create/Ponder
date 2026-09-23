plugins {
    alias(libs.plugins.mdg)
    alias(libs.plugins.configure.platform)
}

neoForge {
    version = libs.versions.neoforge.get()

    runs {
        create("client") {
            client()
        }

        create("server") {
            server()

            gameDirectory = project.file("run/server")
        }

        configureEach {
            jvmArgument("-Dmixin.debug.export=true")
            jvmArgument("-XX:+IgnoreUnrecognizedVMOptions")
            jvmArgument("-XX:+AllowEnhancedClassRedefinition")
        }
    }

    mods.register("catnip") {
        sourceSet(sourceSets.main.get())
    }
}

dependencies {
    api(libs.flywheel.neoforge) {
        capabilities {
            requireFeature("api")
        }
    }
    runtimeOnly(libs.flywheel.neoforge)
}
