package one.devos.nautical.losing_my_marbles.framework.phys;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public final class PhysicsEventListeners {
	public static void tick(Level level) {
		PhysicsEnvironment.get(level).tick();
	}

	public static void entityLoaded(Entity entity, Level level) {
		PhysicsEnvironment.get(level).entityAdded(entity);
	}

	public static void entityUnloaded(Entity entity, Level level) {
		PhysicsEnvironment.get(level).entityRemoved(entity);
	}
}
