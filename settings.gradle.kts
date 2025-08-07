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

// jolt setup. needs a lot because of natives.

// 32-bit platforms are unsupported by MC itself
// some special variants are unimportant:
// - linux + fma
// - windows + avx2
enum class JoltPlatform(val module: String) {
    WINDOWS_64("Windows64"),
    LINUX_64("Linux64"),
    LINUX_ARM("Linux_ARM64"),
    MAC_64("MacOSX64"),
    MAC_ARM64("MacOSX_ARM64");

    companion object {
        fun current(): JoltPlatform {
            val os = System.getProperty("os.name").lowercase(java.util.Locale.ROOT)

            if (os.contains("win")) {
                return WINDOWS_64
            }

            val arch = System.getProperty("os.arch").lowercase(java.util.Locale.ROOT)
            val isArm = arch.startsWith("arm") || arch.startsWith("aarch64")

            return if (arch.contains("mac")) {
                if (isArm) MAC_ARM64 else MAC_64
            } else {
                if (isArm) LINUX_ARM else LINUX_64
            }
        }
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        register("jolt") {
            version("jolt", "2.1.0")
            library("jvm", "com.github.stephengold", "jolt-jni-Windows64").versionRef("jolt")
            library("native", "com.github.stephengold", "jolt-jni-${JoltPlatform.current().module}").versionRef("jolt")

            JoltPlatform.values().forEach {
                val alias = "natives_${it.module.lowercase(java.util.Locale.ROOT)}"
                library(alias, "com.github.stephengold", "jolt-jni-${it.module}").versionRef("jolt")
            }
        }
    }
}
