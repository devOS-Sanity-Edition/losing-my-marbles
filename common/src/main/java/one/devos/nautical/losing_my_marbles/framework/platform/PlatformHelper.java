package one.devos.nautical.losing_my_marbles.framework.platform;

import net.minecraft.world.level.Level;

import one.devos.nautical.losing_my_marbles.framework.phys.PhysicsEnvironment;

public interface PlatformHelper {
	PlatformHelper INSTANCE = Services.load(PlatformHelper.class);

	Env getEnvironment();

	PhysicsEnvironment getPhysicsEnvironment(Level level);
}
