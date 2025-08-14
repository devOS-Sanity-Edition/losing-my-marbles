package one.devos.nautical.losing_my_marbles.framework.phys.terrain.compile;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.github.stephengold.joltjni.Vec3;

import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Cursor3D;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.phys.shapes.VoxelShape;
import one.devos.nautical.losing_my_marbles.framework.phys.terrain.collision.DefaultCollisionSource;
import one.devos.nautical.losing_my_marbles.framework.phys.terrain.collision.PhysicsCollision;
import one.devos.nautical.losing_my_marbles.framework.phys.util.BoxCache;

/**
 * @see SectionCompiler
 */
public final class SectionShapeCompiler implements Supplier<CompiledSection> {
	public static final int SIZE = 16;

	public final Set<BlockPos> triggers;

	private final BoxCache boxCache;
	private final PalettedContainer<BlockState> states;

	public SectionShapeCompiler(BoxCache boxCache, PalettedContainer<BlockState> states, Set<BlockPos> triggers) {
		this.boxCache = boxCache;
		this.states = states;
		this.triggers = triggers;
	}

	@Override
	public CompiledSection get() {
		SectionShapeBuilders builders = new SectionShapeBuilders(this::newBuilder);

		Cursor3D cursor = new Cursor3D(0, 0, 0, SIZE - 1, SIZE - 1, SIZE - 1);

		while (cursor.advance()) {
			BlockState state = this.states.get(cursor.nextX(), cursor.nextY(), cursor.nextZ());

			PhysicsCollision collision = PhysicsCollision.of(state.getBlock());

			if (collision.defaultCollision().isPresent()) {
				DefaultCollisionSource defaultCollision = collision.defaultCollision().get();
				VoxelShape shape = defaultCollision.get(state);
				if (!shape.isEmpty()) {
					builders.get(state).add(cursor.nextX(), cursor.nextY(), cursor.nextZ(), shape);
				}
			}

			collision.provider().build(state, (offset, rotation, shape) -> {
				SectionShape.Builder builder = builders.get(state);
				Vec3 fullOffset = new Vec3(offset);
				fullOffset.addInPlace(cursor.nextX() + 0.5f, cursor.nextY() + 0.5f, cursor.nextZ() + 0.5f);
				builder.add(fullOffset, rotation, shape);
			});
		}

		return new CompiledSection(builders.buildAll(), this.triggers);
	}

	private SectionShape.Builder newBuilder(SectionShape.Properties properties) {
		return new SectionShape.Builder(properties, this.boxCache);
	}

	@Nullable
	public static SectionShapeCompiler create(BoxCache boxCache, LevelChunkSection section, @Nullable CompileTask replaced, @Nullable BlockPos trigger) {
		if (section.hasOnlyAir())
			return null;

		Set<BlockPos> triggers = concatTriggers(replaced, trigger);
		return new SectionShapeCompiler(boxCache, section.getStates().copy(), triggers);
	}

	private static Set<BlockPos> concatTriggers(@Nullable CompileTask replaced, @Nullable BlockPos pos) {
		if (pos == null) {
			return replaced == null ? Set.of() : replaced.compiler().triggers;
		} else if (replaced == null) {
			return Set.of(pos.immutable());
		} else {
			Set<BlockPos> set = new HashSet<>(replaced.compiler().triggers);
			set.add(pos.immutable());
			return set;
		}
	}
}
