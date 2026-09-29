plugins {
    alias(libs.plugins.loom)
    alias(libs.plugins.configure.platform)
}

loom {
    mods.register("testmod") {
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
    api(project(":ponder-fabric"))
    clientCompileOnly(project(":ponder-fabric", configuration = "clientJar"))
    clientCompileOnly(project(":catnip-fabric", configuration = "clientJar"))
}
