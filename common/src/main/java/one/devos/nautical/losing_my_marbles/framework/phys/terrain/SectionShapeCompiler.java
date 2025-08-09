package one.devos.nautical.losing_my_marbles.framework.phys.terrain;

import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;

import net.minecraft.world.level.chunk.PalettedContainer;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

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
		Map<SectionShape.Properties, SectionShape.Builder> builders = new HashMap<>();

		for (int x = 0; x < SIZE; x++) {
			for (int z = 0; z < SIZE; z++) {
				for (int y = 0; y < SIZE; y++) {
					BlockState state = this.states.get(x, y, z);

					VoxelShape shape = state.getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
					if (shape.isEmpty())
						continue;

					SectionShape.Properties properties = SectionShape.Properties.of(state);
					SectionShape.Builder builder = builders.computeIfAbsent(properties, this::newBuilder);

					for (AABB aabb : shape.toAabbs()) {
						builder.add(x, y, z, aabb);
					}
				}
			}
		}

		return new CompiledSection(
				builders.values().stream().map(SectionShape.Builder::build).toList(),
				this.triggers
		);
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
