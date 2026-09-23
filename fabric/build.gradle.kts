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

// runClientRenderDoc doesn't exist on MacOS
//tasks.runClientRenderDoc {
//    renderDocExecutable = file("/usr/bin/renderdoccmd")
//}

dependencies {
    minecraft(libs.minecraft)
    api(libs.bundles.fabric)
    api(project(":catnip:fabric"))
    clientCompileOnly(project(":catnip:fabric", configuration = "clientJar"))
}
