import org.gradle.api.file.DuplicatesStrategy

val compileOnly: Configuration by configurations.getting
val clientCompileOnly: Configuration by configurations.getting

val commonMainJava: Configuration by configurations.dependencyScope("commonMainJava")
val commonMainResources: Configuration by configurations.dependencyScope("commonMainResources")

val commonClientJava: Configuration by configurations.dependencyScope("commonClientJava")
val commonClientResources: Configuration by configurations.dependencyScope("commonClientResources")

val commonPath = project.parent!!.path + ":common"

dependencies {
    compileOnly(project(commonPath))
    clientCompileOnly(project(commonPath, configuration = "commonClientOutput"))

    commonMainJava(project(path = commonPath, configuration = "commonMainJava"))
    commonMainResources(project(path = commonPath, configuration = "commonMainResources"))

    commonClientJava(project(path = commonPath, configuration = "commonClientJava"))
    commonClientResources(project(path = commonPath, configuration = "commonClientResources"))
}

val resolvableCommonMainJava: Configuration by configurations.resolvable("resolvableCommonMainJava") {
    extendsFrom(commonMainJava)
}

val resolvableCommonMainResources: Configuration by configurations.resolvable("resolvableCommonMainResources") {
    extendsFrom(commonMainResources)
}

val resolvableCommonClientJava: Configuration by configurations.resolvable("resolvableCommonClientJava") {
    extendsFrom(commonClientJava)
}

val resolvableCommonClientResources: Configuration by configurations.resolvable("resolvableCommonClientResources") {
    extendsFrom(commonClientResources)
}

val filteredCommonMainJava = resolvableCommonMainJava.asFileTree.matching {
    exclude("**/generatedPackageInfos/net/createmod/catnip/net/base/package-info.java")
    exclude("**/generatedPackageInfos/net/createmod/catnip/api/platform/package-info.java")
    exclude("**/generatedPackageInfos/net/createmod/catnip/api/client/package-info.java")
    exclude("net/createmod/catnip/net/base/package-info.java")
    exclude("net/createmod/catnip/api/platform/package-info.java")
    exclude("net/createmod/catnip/api/client/package-info.java")
}

val filteredCommonClientJava = resolvableCommonClientJava.asFileTree.matching {
    exclude("**/generatedPackageInfos/net/createmod/catnip/net/base/package-info.java")
    exclude("**/generatedPackageInfos/net/createmod/catnip/api/platform/package-info.java")
    exclude("**/generatedPackageInfos/net/createmod/catnip/api/client/package-info.java")
    exclude("net/createmod/catnip/net/base/package-info.java")
    exclude("net/createmod/catnip/api/platform/package-info.java")
    exclude("net/createmod/catnip/api/client/package-info.java")
}

tasks.named<JavaCompile>("compileJava") {
    dependsOn(resolvableCommonMainJava)
    source(filteredCommonMainJava)
}

tasks.named<JavaCompile>("compileClientJava") {
    dependsOn(resolvableCommonClientJava)
    source(filteredCommonClientJava)
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
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    dependsOn(resolvableCommonMainJava, resolvableCommonMainResources)
    from(filteredCommonMainJava, resolvableCommonMainResources)
}
