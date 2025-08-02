// versions
val minecraftVersion = "1.21.1"
val minecraftDep = "=1.21.1"
// https://parchmentmc.org/docs/getting-started
val parchmentVersion = "2024.11.17"
// https://fabricmc.net/develop
val loaderVersion = "0.16.9"
val fapiVersion = "0.114.0+1.21.1"

// dev env mods
// https://modrinth.com/mod/sodium/versions?l=fabric
val sodiumVersion = "mc1.21.1-0.6.5-fabric"
// https://modrinth.com/mod/jade/versions?l=fabric
val jadeVersion = "15.9.2+fabric"
// https://modrinth.com/mod/modmenu/versions
val modmenuVersion = "11.0.3"
// https://modrinth.com/mod/suggestion-tweaker/versions?l=fabric
val suggestionTweakerVersion = "1.20.6-1.5.2+fabric"
// https://modrinth.com/mod/cloth-config/versions?l=fabric
val clothConfigVersion = "15.0.140+fabric"

// buildscript
plugins {
	id("fabric-loom") version "1.9.+"
	id("maven-publish")
}

base.archivesName = "modid"
group = "io.github.tropheusj"

val buildNum = providers.environmentVariable("GITHUB_RUN_NUMBER")
    .filter(String::isNotEmpty)
	.map { "build.$it" }
    .orElse("local")
    .get()

version = "0.1.0+$buildNum-mc$minecraftVersion"

repositories {
	maven("https://maven.parchmentmc.org")
	maven("https://api.modrinth.com/maven")
}

dependencies {
	// dev environment
	minecraft("com.mojang:minecraft:$minecraftVersion")
	mappings(loom.layered {
        officialMojangMappings { nameSyntheticMembers = false }
		parchment("org.parchmentmc.data:parchment-$minecraftVersion:$parchmentVersion@zip")
	})
	modImplementation("net.fabricmc:fabric-loader:$loaderVersion")

	// dependencies
	modImplementation("net.fabricmc.fabric-api:fabric-api:$fapiVersion")

	// dev env
    modLocalRuntime("maven.modrinth:sodium:$sodiumVersion")
    modLocalRuntime("maven.modrinth:jade:$jadeVersion")
    modLocalRuntime("maven.modrinth:modmenu:$modmenuVersion")
	modLocalRuntime("maven.modrinth:suggestion-tweaker:$suggestionTweakerVersion")
	modLocalRuntime("maven.modrinth:cloth-config:$clothConfigVersion")
}

tasks.withType(ProcessResources::class) {
	val properties: Map<String, Any> = mapOf(
		"version" to version,
		"loader_version" to loaderVersion,
		"fapi_version" to fapiVersion,
		"minecraft_dependency" to minecraftDep
	)

	inputs.properties(properties)

	filesMatching("fabric.mod.json") {
		expand(properties)
	}
}

val testmod: SourceSet by sourceSets.creating {
	val main: SourceSet = sourceSets["main"]
	compileClasspath += main.compileClasspath
	compileClasspath += main.output
	runtimeClasspath += main.runtimeClasspath
	runtimeClasspath += main.output
}

loom {
	runs {
		register("testmodClient") {
			client()
			name("Testmod Client")
			source(testmod)
		}
		register("testmodServer") {
			server()
			name("Testmod Server")
			source(testmod)
		}
		register("gametest") {
			server()
			source(testmod)
            ideConfigGenerated(false) // this is meant for CI
            property("fabric-api.gametest")
            property("fabric-api.gametest.report-file=${layout.buildDirectory}/junit.xml")
			runDir("run/gametest_server")
		}
	}
}

java {
	withSourcesJar()
}

publishing {
	publications {
		register<MavenPublication>("mavenJava") {
			from(components["java"])
		}
	}

	repositories {
		maven("https://mvn.devos.one/snapshots") {
			name = "devOsSnapshots"
			credentials(PasswordCredentials::class)
		}
        maven("https://mvn.devos.one/releases") {
            name = "devOsReleases"
            credentials(PasswordCredentials::class)
        }
	}
}
