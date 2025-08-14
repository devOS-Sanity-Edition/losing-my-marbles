package one.devos.nautical.losing_my_marbles.content.marble.data;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

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
}
