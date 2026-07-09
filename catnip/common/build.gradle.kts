plugins {
    alias(libs.plugins.loom)
    alias(libs.plugins.configure.platform)
}

dependencies {
    minecraft(libs.minecraft)
    compileOnly(libs.bundles.mixin)
    compileOnlyApi(libs.bundles.neoforge.config)
    clientCompileOnly(libs.bundles.neoforge.config)
}

loom {
    accessWidenerPath = file("catnip_common_source.accesswidener")
}
