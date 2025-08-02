import fish.cichlidmc.cichlid_gradle.util.Distribution

plugins {
    id("java-library")
    alias(libs.plugins.cichlid.gradle)
}

repositories {
    mavenCentral()
    cichlid.releases()
    minecraft.libraries()
    minecraft.versions()
    // for dist marker
    maven("https://mvn.devos.one/releases/")
}

val mc by minecraft.creating {
    version = libs.versions.minecraft.get()
    distribution = Distribution.MERGED

    runs {
        register("client") {
            client()
        }

        register("server") {
            server()
        }
    }
}

dependencies {
    implementation(mc.dependency)

    compileOnly(cichlid.api(libs.versions.cichlid.asProvider().get()))
    cichlidRuntime(cichlid.runtime(libs.versions.cichlid.asProvider().get()))
}
