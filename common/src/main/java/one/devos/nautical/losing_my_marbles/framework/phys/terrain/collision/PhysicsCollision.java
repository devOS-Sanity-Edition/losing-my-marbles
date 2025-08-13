package one.devos.nautical.losing_my_marbles.framework.phys.terrain.collision;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.github.stephengold.joltjni.Quat;
import com.github.stephengold.joltjni.Vec3;
import com.github.stephengold.joltjni.readonly.ConstShape;
import com.github.stephengold.joltjni.readonly.QuatArg;
import com.github.stephengold.joltjni.readonly.Vec3Arg;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesBlockTags;

/**
 * Defines the collision of a {@link Block} visible to physics objects.
 * @param defaultCollision an optional {@link DefaultCollisionSource} determining how vanilla collision should be interpreted
 */
public record PhysicsCollision(Optional<DefaultCollisionSource> defaultCollision, Provider provider) {
	public static final PhysicsCollision NONE = new PhysicsCollision(Optional.empty(), Provider.NONE);
	public static final PhysicsCollision DEFAULT = new PhysicsCollision(DefaultCollisionSource.COLLISION_SHAPE, Provider.NONE);
	public static final PhysicsCollision DEFAULT_BASE = new PhysicsCollision(DefaultCollisionSource.BASE_SHAPE, Provider.NONE);

	private static final QuatArg noRotation = new Quat(0, 0, 0, 1);

	public PhysicsCollision(@Nullable DefaultCollisionSource defaultCollision, Provider provider) {
		this(Optional.ofNullable(defaultCollision), provider);
	}

	@SuppressWarnings("deprecation") // builtInRegistryHolder
	public static PhysicsCollision of(Block block) {
		PhysicsCollision custom = CustomPhysicsCollisionRegistry.get(block);
		if (custom != null)
			return custom;

		return block.builtInRegistryHolder().is(LosingMyMarblesBlockTags.PHYSICS_USES_BASE_SHAPE) ? DEFAULT_BASE : DEFAULT;
	}

	/**
	 * Provides advanced collision for a given {@link BlockState}. Will be called off the main thread.
	 */
	public interface Provider {
		Provider NONE = (state, output) -> {};

		void build(BlockState state, Output output);

		interface Output {
			/**
			 * Add a shape.
			 * @param offset an offset from the center of the block space
			 */
			void accept(Vec3Arg offset, QuatArg rotation, ConstShape shape);

			default void accept(float xOffset, float yOffset, float zOffset, ConstShape shape) {
				this.accept(xOffset, yOffset, zOffset, noRotation, shape);
			}

			default void accept(float xOffset, float yOffset, float zOffset, QuatArg rotation, ConstShape shape) {
				this.accept(new Vec3(xOffset, yOffset, zOffset), rotation, shape);
			}

			default void accept(ConstShape shape) {
				this.accept(0, 0, 0, shape);
			}
		}
	}
}
