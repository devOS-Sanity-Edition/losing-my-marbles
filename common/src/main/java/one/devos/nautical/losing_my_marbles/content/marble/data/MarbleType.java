package one.devos.nautical.losing_my_marbles.content.marble.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFileCodec;
import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesRegistries;

public record MarbleType(DataComponentMap components) {
	public static final Codec<MarbleType> DIRECT_CODEC = RecordCodecBuilder.create(i -> i.group(
			DataComponentMap.CODEC.fieldOf("components").forGetter(MarbleType::components)
	).apply(i, MarbleType::new));

	public static final Codec<Holder<MarbleType>> CODEC = RegistryFileCodec.create(LosingMyMarblesRegistries.MARBLE_TYPE, DIRECT_CODEC);
	public static final StreamCodec<RegistryFriendlyByteBuf, Holder<MarbleType>> STREAM_CODEC = ByteBufCodecs.holderRegistry(LosingMyMarblesRegistries.MARBLE_TYPE);
}
