plugins {
    alias(libs.plugins.mdg)
    alias(libs.plugins.configure.platform)
}

neoForge {
    version = libs.versions.neoforge.get()

    accessTransformers.from(file("src/main/resources/META-INF/accesstransformer.cfg"))

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

    mods.register("ponder") {
        sourceSet(sourceSets.main.get())
    }
}

dependencies {
    api(project(":catnip:neoforge"))
    api(libs.flywheel.neoforge) {
        capabilities {
            requireFeature("api")
        }
    }
    runtimeOnly(libs.flywheel.neoforge)
}
