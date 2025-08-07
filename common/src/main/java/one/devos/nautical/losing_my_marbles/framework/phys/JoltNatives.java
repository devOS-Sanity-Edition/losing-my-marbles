package one.devos.nautical.losing_my_marbles.framework.phys;

import one.devos.nautical.losing_my_marbles.framework.platform.PlatformHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public final class JoltNatives {
	public static final String FOLDER = "jolt_natives";
	public static final String EXTRACTED_FOLDER = "losing_my_marbles_jolt_natives";

	public static void load() {
		Path extracted = PlatformHelper.INSTANCE.getGameDir().resolve(EXTRACTED_FOLDER);
		Platform platform = Platform.current();
		Path lib = extracted.resolve(platform.filename);
		ensureExtracted(platform, lib);

		String pathString = lib.toAbsolutePath().toString();
		System.load(pathString);
	}

	private static void ensureExtracted(Platform platform, Path dest) {
		if (Files.exists(dest))
			return;

		Path natives = PlatformHelper.INSTANCE.findPath(FOLDER).orElseThrow(
				() -> new IllegalStateException("Jolt natives are missing from the jar")
		);

		Path src = natives.resolve(platform.filename);
		if (!Files.exists(src)) {
			throw new IllegalStateException("Jolt native for platform is missing from jar: " + platform);
		}

		try {
			Files.createDirectories(dest.getParent());
			Files.copy(src, dest);
		} catch (IOException e) {
			throw new RuntimeException("Failed to extract Jolt native for " + platform, e);
		}
	}

	private enum Platform {
		WINDOWS_64("Windows64"),
		LINUX_64("Linux64"),
		LINUX_ARM("Linux_ARM64"),
		MAC_64("MacOSX64"),
		MAC_ARM64("MacOSX_ARM64");

		private final String filename;

		Platform(String filename) {
			this.filename = filename;
		}

		private static Platform current() {
			String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);

			if (os.contains("win")) {
				return WINDOWS_64;
			}

			String arch = System.getProperty("os.arch").toLowerCase(Locale.ROOT);
			boolean isArm = arch.startsWith("arm") || arch.startsWith("aarch64");

			if (arch.contains("mac")) {
				return isArm ? MAC_ARM64 : MAC_64;
			} else {
				return isArm ? LINUX_ARM : LINUX_64;
			}
		}
	}
}
