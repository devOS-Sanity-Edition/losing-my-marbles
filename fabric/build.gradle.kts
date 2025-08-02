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
