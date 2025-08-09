package one.devos.nautical.losing_my_marbles.framework.phys;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.shapes.VoxelShape;

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

	public static void chunkStatusChanged(LevelChunk chunk, FullChunkStatus oldStatus, FullChunkStatus newStatus) {
		if (collides(oldStatus) && !collides(newStatus)) {
			// unloaded
			PhysicsEnvironment.get(chunk.getLevel()).chunkUnloaded(chunk);
		} else if (!collides(oldStatus) && collides(newStatus)) {
			// loaded
			PhysicsEnvironment.get(chunk.getLevel()).chunkLoaded(chunk);
		}
	}

	public static void blockChanged(Level level, BlockPos pos, BlockState oldState, BlockState newState) {
		if (oldState == newState)
			return;

		VoxelShape oldShape = PhysicsEnvironment.getPhysicsVisibleShape(oldState);
		VoxelShape newShape = PhysicsEnvironment.getPhysicsVisibleShape(newState);

		if (oldShape != newShape) {
			PhysicsEnvironment.get(level).blockShapeChanged(pos);
		}
	}

	private static boolean collides(FullChunkStatus status) {
		return status.isOrAfter(FullChunkStatus.ENTITY_TICKING);
	}
}
