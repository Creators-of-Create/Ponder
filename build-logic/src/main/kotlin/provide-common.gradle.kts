configurations {
    consumable("commonMainJava")
    consumable("commonMainResources")
    consumable("commonClientJava")
    consumable("commonClientResources")
    consumable("commonClientOutput")
}

val sourceSets = extensions.getByType<SourceSetContainer>()

artifacts {
    sourceSets["main"].run {
        java.sourceDirectories.forEach { add("commonMainJava", it) }
        resources.sourceDirectories.forEach { add("commonMainResources", it) }
    }

    sourceSets["client"].run {
        java.sourceDirectories.forEach { add("commonClientJava", it) }
        resources.sourceDirectories.forEach { add("commonClientResources", it) }

        output.forEach {
            add("commonClientOutput", it) {
                // this one doesn't preserve task dependencies for some reason, add them manually
                builtBy(tasks.named("compileClientJava"), tasks.named("processClientResources"))
            }
        }
    }
}
