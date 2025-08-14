package one.devos.nautical.losing_my_marbles.framework.phys.terrain.collision;

import java.util.IdentityHashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.level.block.Block;

/**
 * Registry mapping {@link Block}s to custom {@link PhysicsCollision} values.
 */
public final class CustomPhysicsCollisionRegistry {
	private static final Map<Block, PhysicsCollision> registry = new IdentityHashMap<>();

	@Nullable
	public static PhysicsCollision get(Block block) {
		return registry.get(block);
	}

	public static void register(Block block, PhysicsCollision collision) {
		PhysicsCollision existing = registry.get(block);
		if (existing != null) {
			throw new RuntimeException("Advanced collision has already been registered for " + block + ": " + existing);
		}

		registry.put(block, collision);
	}

	public static void register(Block block, @Nullable DefaultCollisionSource defaultCollision, PhysicsCollision.Provider provider) {
		register(block, new PhysicsCollision(defaultCollision, provider));
	}
}
