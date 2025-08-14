package one.devos.nautical.losing_my_marbles.content;

import com.mojang.serialization.Codec;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import one.devos.nautical.losing_my_marbles.LosingMyMarbles;
import one.devos.nautical.losing_my_marbles.content.marble.data.MarbleInstance;
import one.devos.nautical.losing_my_marbles.content.marble.data.shape.MarbleShape;
import one.devos.nautical.losing_my_marbles.content.marble.data.texture.MarbleTexture;
import one.devos.nautical.losing_my_marbles.framework.data.LosingMyMarblesCodecs;
import one.devos.nautical.losing_my_marbles.framework.network.LosingMyMarblesStreamCodecs;

public final class LosingMyMarblesDataComponents {
	// for items
	public static final DataComponentType<MarbleInstance> MARBLE = register(
			"marble", MarbleInstance.CODEC, MarbleInstance.STREAM_CODEC
	);
	// for marbles
	public static final DataComponentType<Float> FRICTION = register(
			"friction", LosingMyMarblesCodecs.NORMALIZED_FLOAT, LosingMyMarblesStreamCodecs.NORMALIZED_FLOAT
	);
	public static final DataComponentType<Float> RESTITUTION = register(
			"restitution", LosingMyMarblesCodecs.NORMALIZED_FLOAT, LosingMyMarblesStreamCodecs.NORMALIZED_FLOAT
	);
	public static final DataComponentType<Float> SCALE = register(
			"scale", Codec.floatRange(1 / 16f, 2), LosingMyMarblesStreamCodecs.floatRange(1 / 16f, 2)
	);
	public static final DataComponentType<Holder<SoundEvent>> BOUNCE_SOUND = register(
			"bounce_sound", SoundEvent.CODEC, SoundEvent.STREAM_CODEC
	);
	public static final DataComponentType<MarbleShape> SHAPE = register(
			"shape", MarbleShape.CODEC, MarbleShape.STREAM_CODEC
	);
	public static final DataComponentType<MarbleTexture> TEXTURE = register(
			"texture", MarbleTexture.CODEC, MarbleTexture.STREAM_CODEC
	);

	public static void init() {
	}

	private static <T> DataComponentType<T> register(String name, Codec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
		ResourceLocation id = LosingMyMarbles.id(name);
		DataComponentType<T> type = DataComponentType.<T>builder().persistent(codec).networkSynchronized(streamCodec).build();
		return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, id, type);
	}
}
