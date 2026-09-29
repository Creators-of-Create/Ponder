val compileOnly: Configuration = configurations["compileOnly"]
val clientCompileOnly: Configuration = configurations["clientCompileOnly"]

val commonMainJava: Configuration = configurations.dependencyScope("commonMainJava").get()
val commonMainResources: Configuration = configurations.dependencyScope("commonMainResources").get()

val commonClientJava: Configuration = configurations.dependencyScope("commonClientJava").get()
val commonClientResources: Configuration = configurations.dependencyScope("commonClientResources").get()

val commonPath = ":${project.name.substringBeforeLast("-")}-common"

dependencies {
    compileOnly(project(commonPath))
    clientCompileOnly(project(commonPath, configuration = "commonClientOutput"))

    commonMainJava(project(path = commonPath, configuration = "commonMainJava"))
    commonMainResources(project(path = commonPath, configuration = "commonMainResources"))

    commonClientJava(project(path = commonPath, configuration = "commonClientJava"))
    commonClientResources(project(path = commonPath, configuration = "commonClientResources"))
}

val resolvableCommonMainJava = configurations.resolvable("resolvableCommonMainJava") {
    extendsFrom(commonMainJava)
}

val resolvableCommonMainResources = configurations.resolvable("resolvableCommonMainResources") {
    extendsFrom(commonMainResources)
}

val resolvableCommonClientJava = configurations.resolvable("resolvableCommonClientJava") {
    extendsFrom(commonClientJava)
}

val resolvableCommonClientResources = configurations.resolvable("resolvableCommonClientResources") {
    extendsFrom(commonClientResources)
}

tasks.named<JavaCompile>("compileJava") {
    dependsOn(resolvableCommonMainJava)
    source(resolvableCommonMainJava)
}

tasks.named<JavaCompile>("compileClientJava") {
    dependsOn(resolvableCommonClientJava)
    source(resolvableCommonClientJava)
}

tasks.named<ProcessResources>("processResources") {
    dependsOn(resolvableCommonMainResources)
    from(resolvableCommonMainResources)
}

tasks.named<ProcessResources>("processClientResources") {
    dependsOn(resolvableCommonClientResources)
    from(resolvableCommonClientResources)
}

tasks.named<Jar>("sourcesJar") {
    dependsOn(resolvableCommonMainJava, resolvableCommonMainResources)
    from(resolvableCommonMainJava, resolvableCommonMainResources)
}
