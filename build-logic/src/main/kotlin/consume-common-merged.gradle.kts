import org.gradle.api.file.DuplicatesStrategy

val compileOnly: Configuration by configurations.getting
val commonJava: Configuration by configurations.dependencyScope("commonJava")
val commonResources: Configuration by configurations.dependencyScope("commonResources")

val commonPath = project.parent!!.path + ":common"

dependencies {
    compileOnly(project(commonPath))
    compileOnly(project(commonPath, configuration = "commonClientOutput"))
    commonJava(project(path = commonPath, configuration = "commonMainJava"))
    commonJava(project(path = commonPath, configuration = "commonClientJava"))
    commonResources(project(path = commonPath, configuration = "commonMainResources"))
    commonResources(project(path = commonPath, configuration = "commonClientResources"))
}

val resolvableCommonJava: Configuration by configurations.resolvable("resolvableCommonJava") {
    extendsFrom(commonJava)
}

val resolvableCommonResources: Configuration by configurations.resolvable("resolvableCommonResources") {
    extendsFrom(commonResources)
}

val filteredCommonJava = resolvableCommonJava.asFileTree.matching {
    exclude("**/generatedPackageInfos/net/createmod/catnip/net/base/package-info.java")
    exclude("**/generatedPackageInfos/net/createmod/catnip/api/platform/package-info.java")
    exclude("**/generatedPackageInfos/net/createmod/catnip/api/client/package-info.java")
    exclude("net/createmod/catnip/net/base/package-info.java")
    exclude("net/createmod/catnip/api/platform/package-info.java")
    exclude("net/createmod/catnip/api/client/package-info.java")
}

tasks.named<JavaCompile>("compileJava") {
    dependsOn(resolvableCommonJava)
    source(filteredCommonJava)
}

tasks.named<ProcessResources>("processResources") {
    dependsOn(resolvableCommonResources)
    from(resolvableCommonResources)
}

tasks.named<Jar>("sourcesJar") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    dependsOn(resolvableCommonJava, resolvableCommonResources)
    from(filteredCommonJava, resolvableCommonResources)
}
