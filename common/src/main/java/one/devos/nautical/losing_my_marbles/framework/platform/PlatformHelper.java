package one.devos.nautical.losing_my_marbles.framework.platform;

import net.minecraft.server.level.ServerLevel;

import one.devos.nautical.losing_my_marbles.framework.phys.PhysicsEnvironment;

import java.nio.file.Path;
import java.util.Optional;

public interface PlatformHelper {
	PlatformHelper INSTANCE = Services.load(PlatformHelper.class);

	Env getEnvironment();

	Path getGameDir();

	Optional<Path> findPath(String name);

	PhysicsEnvironment getPhysicsEnvironment(ServerLevel level);
}
