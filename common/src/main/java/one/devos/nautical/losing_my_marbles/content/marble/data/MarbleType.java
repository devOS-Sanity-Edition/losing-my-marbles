package one.devos.nautical.losing_my_marbles.content.marble.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import one.devos.nautical.losing_my_marbles.LosingMyMarbles;
import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesRegistries;

public record MarbleType(Component name, DataComponentMap components) implements DataComponentHolder {
	public static final ResourceKey<MarbleType> DEFAULT = ResourceKey.create(LosingMyMarblesRegistries.MARBLE_TYPE, LosingMyMarbles.id("terracotta"));

	public static final Codec<MarbleType> DIRECT_CODEC = RecordCodecBuilder.create(i -> i.group(
			ComponentSerialization.CODEC.fieldOf("name").forGetter(MarbleType::name),
			DataComponentMap.CODEC.fieldOf("components").forGetter(MarbleType::components)
	).apply(i, MarbleType::new));

	public static final Codec<Holder<MarbleType>> CODEC = RegistryFileCodec.create(LosingMyMarblesRegistries.MARBLE_TYPE, DIRECT_CODEC);
	public static final StreamCodec<RegistryFriendlyByteBuf, Holder<MarbleType>> STREAM_CODEC = ByteBufCodecs.holderRegistry(LosingMyMarblesRegistries.MARBLE_TYPE);

	@Override
	public DataComponentMap getComponents() {
		return this.components;
	}
}
