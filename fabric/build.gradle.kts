plugins {
    alias(libs.plugins.loom)
    alias(libs.plugins.configure.platform)
}

loom {
    mods.register("ponder") {
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
    api(libs.bundles.fabric)
    api(project(":catnip-fabric"))
    clientCompileOnly(project(":catnip-fabric", configuration = "clientJar"))
}
