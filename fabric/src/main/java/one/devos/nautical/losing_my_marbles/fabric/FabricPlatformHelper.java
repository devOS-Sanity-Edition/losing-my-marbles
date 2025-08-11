package one.devos.nautical.losing_my_marbles.fabric;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerLevel;
import one.devos.nautical.losing_my_marbles.LosingMyMarbles;
import one.devos.nautical.losing_my_marbles.framework.phys.PhysicsEnvironment;
import one.devos.nautical.losing_my_marbles.framework.platform.Env;
import one.devos.nautical.losing_my_marbles.framework.platform.PlatformHelper;

import java.nio.file.Path;
import java.util.Optional;

public class FabricPlatformHelper implements PlatformHelper {
	@Override
	public Env getEnvironment() {
		return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT ? Env.CLIENT : Env.SERVER;
	}

	@Override
	public Path getGameDir() {
		return FabricLoader.getInstance().getGameDir();
	}

	@Override
	public Optional<Path> findPath(String name) {
		return FabricLoader.getInstance().getModContainer(LosingMyMarbles.ID).orElseThrow().findPath(name);
	}

	@Override
	public PhysicsEnvironment getPhysicsEnvironment(ServerLevel level) {
		return level.getAttachedOrCreate(LosingMyMarblesFabric.PHYSICS_ENV_ATTACHMENT, () -> new PhysicsEnvironment(level));
	}
}
