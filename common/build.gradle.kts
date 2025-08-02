import fish.cichlidmc.cichlid_gradle.util.Distribution

plugins {
    id("java-library")
    alias(libs.plugins.cichlid.gradle)
}

repositories {
    mavenCentral()
    minecraft.libraries()
    minecraft.versions()
    // for dist marker
    maven("https://mvn.devos.one/releases/")
}

val mc by minecraft.creating {
    version = libs.versions.minecraft.get()
    distribution = Distribution.MERGED
}

dependencies {
    implementation(mc.dependency)
}

// outgoing configurations for loader projects

configurations.consumable("commonJava")
configurations.consumable("commonResources")

artifacts {
    add("commonJava", sourceSets.main.get().java.sourceDirectories.singleFile)
    add("commonResources", sourceSets.main.get().resources.sourceDirectories.singleFile)
}
