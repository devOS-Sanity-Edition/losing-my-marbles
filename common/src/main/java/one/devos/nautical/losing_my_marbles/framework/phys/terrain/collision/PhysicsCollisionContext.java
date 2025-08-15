package one.devos.nautical.losing_my_marbles.framework.phys.terrain.collision;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Collision context provided to {@code getCollisionShape} when getting the shape to use for physics bodies.
 */
public enum PhysicsCollisionContext implements CollisionContext {
	INSTANCE;

	@Override
	public boolean isDescending() {
		return false;
	}

	@Override
	public boolean isAbove(VoxelShape var1, BlockPos var2, boolean var3) {
		return false;
	}

	@Override
	public boolean isHoldingItem(Item item) {
		return false;
	}

	@Override
	public boolean canStandOnFluid(FluidState fluid1, FluidState fluid2) {
		return false;
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, CollisionGetter level, BlockPos pos) {
		return state.getCollisionShape(level, pos, this);
	}
}
