package one.devos.nautical.losing_my_marbles.content.marble.data;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesRegistries;

public record MarbleInstance(Holder<MarbleType> typeHolder, PatchedDataComponentMap components) implements DataComponentHolder {
	public static final Codec<MarbleInstance> CODEC = RecordCodecBuilder.create(i -> i.group(
			MarbleType.CODEC.fieldOf("type").forGetter(MarbleInstance::typeHolder),
			DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(MarbleInstance::componentPatch)
	).apply(i, MarbleInstance::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, MarbleInstance> STREAM_CODEC = StreamCodec.composite(
			MarbleType.STREAM_CODEC, MarbleInstance::typeHolder,
			DataComponentPatch.STREAM_CODEC, MarbleInstance::componentPatch,
			MarbleInstance::new
	);

	public MarbleInstance(Holder<MarbleType> type, DataComponentPatch patch) {
		this(type, PatchedDataComponentMap.fromPatch(type.value().components(), patch));
	}

	public MarbleInstance(Holder<MarbleType> type) {
		this(type, DataComponentPatch.EMPTY);
	}

	public MarbleInstance copy() {
		return new MarbleInstance(this.typeHolder, this.components.copy());
	}

	public MarbleType type() {
		return this.typeHolder.value();
	}

	@Override
	public DataComponentMap getComponents() {
		return this.components;
	}

	public DataComponentPatch componentPatch() {
		return this.components.asPatch();
	}

	@Nullable
	public <T> T set(DataComponentType<T> type, @Nullable T value) {
		return this.components.set(type, value);
	}

	public <T> Optional<T> getOptional(DataComponentType<T> type) {
		return Optional.ofNullable(this.get(type));
	}

	// utilities from ItemStack

	public DataComponentMap getPrototype() {
		return this.type().components();
	}

	public DataComponentPatch getComponentsPatch() {
		return this.components.asPatch();
	}

	public DataComponentMap immutableComponents() {
		return this.components.toImmutableMap();
	}

	public boolean hasNonDefault(DataComponentType<?> type) {
		return this.components.hasNonDefault(type);
	}

	public static MarbleInstance getDefault(HolderLookup.Provider registries) {
		HolderLookup.RegistryLookup<MarbleType> registry = registries.lookupOrThrow(LosingMyMarblesRegistries.MARBLE_TYPE);
		Holder.Reference<MarbleType> type = registry.get(MarbleType.DEFAULT).or(() -> registry.listElements().findFirst()).orElseThrow();
		return new MarbleInstance(type, DataComponentPatch.EMPTY);
	}
}
