// kinda gross, but this configuration needs to happen after plugins are loaded
evaluationDependsOnChildren()

subprojects {
    // set the archive name to the mod ID
    extensions.getByType<BasePluginExtension>().archivesName = "losing_my_marbles"
    group = "one.devos.nautical"

    val buildNum = providers.environmentVariable("GITHUB_RUN_NUMBER")
        .filter(String::isNotEmpty)
        .map { "build.$it" }
        .orElse("local")
        .get()

    // x.y.z+build.100-mc1.21.8-fabric
    version = "0.1.0+$buildNum-mc${libs.versions.minecraft.get()}-$name"

    extensions.getByType<JavaPluginExtension>().run {
        // enable the sources jar
        withSourcesJar()
        // and set java requirement
        toolchain.languageVersion = JavaLanguageVersion.of(21)
    }

    if (name == "common") {
        // further configuration is exclusive to loader projects
        return@subprojects
    }

    // expand properties in metadata
    tasks.named<ProcessResources>("processResources") {
        val properties: Map<String, Any> = mapOf(
            "version" to version,
            "loader_version" to libs.versions.fabric.loader.get(),
            "fapi_version" to libs.versions.fabric.api.get(),
            "minecraft_version" to libs.versions.minecraft.get()
        )

        inputs.properties(properties)

        filesMatching(listOf("fabric.mod.json", "cichlid.mod.json")) {
            expand(properties)
        }
    }

    // configurations used to include common code in built artifacts
    val commonJava: Configuration by configurations.dependencyScope("commonJava")
    val commonResources: Configuration by configurations.dependencyScope("commonResources")

    val compileOnly: Configuration = configurations.getByName("compileOnly")

    dependencies {
        compileOnly(project(":common"))
        commonJava(project(path = ":common", configuration = "commonJava"))
        commonResources(project(path = ":common", configuration = "commonResources"))
    }

    // include common stuff in assembly tasks

    val resolvableCommonJava: Configuration by configurations.resolvable("resolvableCommonJava") {
        extendsFrom(commonJava)
    }
    val resolvableCommonResources: Configuration by configurations.resolvable("resolvableCommonResources") {
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
        dependsOn(resolvableCommonJava)
        from(resolvableCommonJava)
        dependsOn(resolvableCommonResources)
        from(resolvableCommonResources)
    }
}
