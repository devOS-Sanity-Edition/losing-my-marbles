plugins {
    alias(libs.plugins.loom)
}

repositories {
	maven("https://api.modrinth.com/maven")
}

dependencies {
	minecraft(libs.fabric.minecraft)
	mappings(loom.officialMojangMappings())
    modImplementation(libs.bundles.fabric)
    modLocalRuntime(libs.bundles.fabric.dev)

    modLocalRuntime(variantOf(jolt.runtime) {
        classifier("DebugDp")
    })

    // this is stupid but a bundle can't be used here. I tried for an hour.
    include(release(jolt.natives.windows64))
    include(release(jolt.natives.linux64))
    include(release(jolt.natives.linux.arm64))
    include(release(jolt.natives.macosx64))
    include(release(jolt.natives.macosx.arm64))
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
    }
}
