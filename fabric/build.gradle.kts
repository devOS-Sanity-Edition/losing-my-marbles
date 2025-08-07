plugins {
    alias(libs.plugins.loom)
}

repositories {
	maven("https://api.modrinth.com/maven")
}

val joltNative: Configuration by configurations.dependencyScope("joltNative")

dependencies {
	minecraft(libs.fabric.minecraft)
	mappings(loom.officialMojangMappings())
    modImplementation(libs.bundles.fabric)
    modLocalRuntime(libs.bundles.fabric.dev)

    implementation(jolt.jvm)
    modLocalRuntime(variantOf(jolt.native) {
        classifier("DebugDp")
    })

    // this is stupid but a bundle can't be used here. I tried for an hour.
    joltNative(release(jolt.natives.windows64))
    joltNative(release(jolt.natives.linux64))
    joltNative(release(jolt.natives.linux.arm64))
    joltNative(release(jolt.natives.macosx64))
    joltNative(release(jolt.natives.macosx.arm64))
}

fun DependencyHandler.release(native: Provider<MinimalExternalModuleDependency>): Provider<MinimalExternalModuleDependency> {
    return this.variantOf(native) {
        classifier("ReleaseDp")
    }
}

loom.runs {
    named("client") {
        configName = "Fabric Client"
    }
    named("server") {
        configName = "Fabric Server"
    }

    configureEach {
        // this defaults to false for subprojects
        isIdeConfigGenerated = true
        // no need
        appendProjectPathToConfigName = false
        // enable assertions in Jolt
        vmArg("-ea")
    }
}

val resolvableJoltNatives: Configuration by configurations.resolvable("resolvableJoltNatives") {
    extendsFrom(joltNative)
}

tasks.named<ProcessResources>("processResources") {
    dependsOn(resolvableJoltNatives)

    resolvableJoltNatives.forEach { jar ->
        val platform = jar.name.substring("jolt-jni-".length).substringBefore("-")
        zipTree(jar).forEach { entry ->
            from(entry) {
                into("jolt_natives")
                rename { platform }
                include("**/*.dll")
                include("**/*.so")
                include("**/*.dylib")
            }
        }
    }
}
