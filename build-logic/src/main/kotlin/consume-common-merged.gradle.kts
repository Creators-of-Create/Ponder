val compileOnly: Configuration = configurations["compileOnly"]
val commonJava: Configuration = configurations.dependencyScope("commonJava").get()
val commonResources: Configuration = configurations.dependencyScope("commonResources").get()

val commonPath = ":${project.name.substringBeforeLast("-")}-common"

dependencies {
    compileOnly(project(commonPath))
    compileOnly(project(commonPath, configuration = "commonClientOutput"))

    commonJava(project(path = commonPath, configuration = "commonMainJava"))
    commonJava(project(path = commonPath, configuration = "commonClientJava"))

    commonResources(project(path = commonPath, configuration = "commonMainResources"))
    commonResources(project(path = commonPath, configuration = "commonClientResources"))
}

val resolvableCommonJava = configurations.resolvable("resolvableCommonJava") {
    extendsFrom(commonJava)
}

val resolvableCommonResources = configurations.resolvable("resolvableCommonResources") {
    extendsFrom(commonResources)
}

tasks.named<JavaCompile>("compileJava") {
    dependsOn(resolvableCommonJava)
    source(resolvableCommonJava)
}

tasks.named<ProcessResources>("processResources") {
    dependsOn(resolvableCommonResources)
    from(resolvableCommonResources)
}

tasks.named<Jar>("sourcesJar") {
    dependsOn(resolvableCommonJava, resolvableCommonResources)
    from(resolvableCommonJava, resolvableCommonResources)
}
