// versions
val minecraftVersion = "1.21.8"
val minecraftDep = "=1.21.8"
// https://parchmentmc.org/docs/getting-started
val parchmentVersion = "none"
// https://fabricmc.net/develop
val loaderVersion = "0.16.14"
val fapiVersion = "0.130.0+1.21.8"

// dev env mods
// https://modrinth.com/mod/sodium/versions?l=fabric
val sodiumVersion = "mc1.21.6-0.6.13-fabric"
// https://modrinth.com/mod/modmenu/versions
val modmenuVersion = "15.0.0-beta.3"

// buildscript
plugins {
	id("fabric-loom") version "1.11.+"
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
	mappings(loom.officialMojangMappings())// { nameSyntheticMembers = false })
	modImplementation("net.fabricmc:fabric-loader:$loaderVersion")

	// dependencies
	modImplementation("net.fabricmc.fabric-api:fabric-api:$fapiVersion")

	// dev env
    modLocalRuntime("maven.modrinth:sodium:$sodiumVersion")
    modLocalRuntime("maven.modrinth:modmenu:$modmenuVersion")
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
