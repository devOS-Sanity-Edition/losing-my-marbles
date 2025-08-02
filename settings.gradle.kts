enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

pluginManagement {
	repositories {
        mavenCentral()
		maven("https://maven.fabricmc.net/" )
        maven("https://mvn.devos.one/releases/")
        maven("https://mvn.devos.one/snapshots/")
	}
}

include("cichlid", "common", "fabric")
