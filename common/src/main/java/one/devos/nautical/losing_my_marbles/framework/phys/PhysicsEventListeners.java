package one.devos.nautical.losing_my_marbles.framework.phys;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class PhysicsEventListeners {
	public static void tick(ServerLevel level) {
		PhysicsEnvironment.get(level).tick();
	}

	public static void entityLoaded(Entity entity, ServerLevel level) {
		PhysicsEnvironment.get(level).entityAdded(entity);
	}

	public static void entityUnloaded(Entity entity, ServerLevel level) {
		PhysicsEnvironment.get(level).entityRemoved(entity);
	}

	public static void chunkStatusChanged(LevelChunk chunk, FullChunkStatus oldStatus, FullChunkStatus newStatus) {
		if (!(chunk.getLevel() instanceof ServerLevel level)) {
			throw new IllegalStateException("chunkStatusChanged called from client");
		}

		if (collides(oldStatus) && !collides(newStatus)) {
			// unloaded
			PhysicsEnvironment.get(level).chunkUnloaded(chunk);
		} else if (!collides(oldStatus) && collides(newStatus)) {
			// loaded
			PhysicsEnvironment.get(level).chunkLoaded(chunk);
		}
	}

	public static void blockChanged(ServerLevel level, BlockPos pos, BlockState oldState, BlockState newState) {
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
