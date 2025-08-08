package one.devos.nautical.losing_my_marbles.framework.phys.terrain;

import com.github.stephengold.joltjni.BodyCreationSettings;

import com.github.stephengold.joltjni.BodyIdArray;
import com.github.stephengold.joltjni.readonly.ConstShapeSettings;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import one.devos.nautical.losing_my_marbles.framework.phys.BodyAccess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TerrainCollision {
	private final Level level;
	private final BodyAccess.Factory factory;

	private final Map<BlockPos, BlockBody> blockBodies;
	private final List<BlockBody> createdLastStep;

	public TerrainCollision(Level level, BodyAccess.Factory factory) {
		this.level = level;
		this.factory = factory;
		this.blockBodies = new HashMap<>();
		this.createdLastStep = new ArrayList<>();
	}

	public void stepArea(AABB area) {
		// pos is a reused mutable object
		for (BlockPos pos : BlockPos.betweenClosed(area)) {
			BlockState state = this.level.getBlockState(pos);

			BlockBody cached = this.blockBodies.get(pos);
			if (cached != null && cached.cacheable && cached.state == state) {
				// cached and still valid.
				cached.refresh();
				continue;
			}

			// not seen, generate and cache

			ConstShapeSettings shape = BlockShapeFactory.create(this.level, state, pos);
			if (shape == null)
				continue;

			boolean cacheable = !state.getBlock().hasDynamicShape();

			BodyCreationSettings settings = new BodyCreationSettings();
			settings.setShapeSettings(shape);
			settings.setPosition(pos.getX(), pos.getY(), pos.getZ());
			BlockBodySettings.configure(settings, state);

			BodyAccess access = this.factory.create(settings);
			BlockBody body = new BlockBody(state, cacheable, access);

			BlockPos immutablePos = pos.immutable();
			this.blockBodies.put(immutablePos, body);
			this.createdLastStep.add(body);
		}
	}

	public void postStep() {
		this.blockBodies.entrySet().removeIf(entry -> {
			BlockBody body = entry.getValue();
			body.tick();
			if (body.shouldDiscard()) {
				body.access.discard();
				return true;
			}

			return false;
		});

		this.createdLastStep.clear();
	}

	/**
	 * Invoke the given consumer with the set of bodies that were created last step.
	 */
	public void forNewBodies(NewBodiesConsumer consumer) {
		if (this.createdLastStep.isEmpty())
			return;

		int size = this.createdLastStep.size();
		BodyIdArray array = new BodyIdArray(size);
		for (int i = 0; i < size; i++) {
			array.set(i, this.createdLastStep.get(i).access.id());
		}

		consumer.accept(size, array);
	}

	public interface NewBodiesConsumer {
		void accept(int size, BodyIdArray ids);
	}

	private static final class BlockBody {
		public static final int INITIAL_LIFETIME = 5;

		private final BlockState state;
		private final boolean cacheable;
		private final BodyAccess access;
		private int lifetime = INITIAL_LIFETIME;

		private BlockBody(BlockState state, boolean cacheable, BodyAccess access) {
			this.state = state;
			this.cacheable = cacheable;
			this.access = access;
		}

		private void tick() {
			this.lifetime--;
		}

		private boolean shouldDiscard() {
			return !this.cacheable || this.lifetime <= 0;
		}

		private void refresh() {
			this.lifetime = INITIAL_LIFETIME;
		}
	}
}
