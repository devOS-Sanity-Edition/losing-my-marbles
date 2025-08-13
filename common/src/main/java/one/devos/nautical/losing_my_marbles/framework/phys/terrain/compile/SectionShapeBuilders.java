package one.devos.nautical.losing_my_marbles.framework.phys.terrain.compile;

import java.util.List;
import java.util.function.Function;

import it.unimi.dsi.fastutil.floats.Float2ObjectMap;
import it.unimi.dsi.fastutil.floats.Float2ObjectOpenHashMap;
import net.minecraft.world.level.block.state.BlockState;

public final class SectionShapeBuilders {
	private final Function<SectionShape.Properties, SectionShape.Builder> builderFactory;

	// Map<friction, Map<restitution, Builder>
	private final Float2ObjectMap<Float2ObjectMap<SectionShape.Builder>> builders;

	public SectionShapeBuilders(Function<SectionShape.Properties, SectionShape.Builder> builderFactory) {
		this.builderFactory = builderFactory;
		this.builders = new Float2ObjectOpenHashMap<>();
	}

	public SectionShape.Builder get(BlockState state) {
		float friction = state.getBlock().getFriction();
		float restitution = 0.5f;// TODO: custom restitution values
		return this.builders.computeIfAbsent(friction, $ -> new Float2ObjectOpenHashMap<>())
				.computeIfAbsent(restitution, $ -> this.builderFactory.apply(new SectionShape.Properties(friction, restitution)));
	}

	public List<SectionShape> buildAll() {
		return this.builders.values().stream()
				.flatMap(map -> map.values().stream())
				.map(SectionShape.Builder::build)
				.toList();
	}
}
