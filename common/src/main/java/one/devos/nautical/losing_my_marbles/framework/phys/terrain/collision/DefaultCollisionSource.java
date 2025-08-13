package one.devos.nautical.losing_my_marbles.framework.phys.terrain.collision;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Possible sources for a block's default collision.
 */
public enum DefaultCollisionSource {
	COLLISION_SHAPE {
		@Override
		public VoxelShape get(BlockState state) {
			return state.getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
		}
	},
	BASE_SHAPE {
		@Override
		public VoxelShape get(BlockState state) {
			return state.getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
		}
	};

	public abstract VoxelShape get(BlockState state);
}
