package one.devos.nautical.losing_my_marbles.content.marble.data.texture;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesRegistries;
import one.devos.nautical.losing_my_marbles.content.marble.MarbleEntity;

public interface MarbleTexture {
	Codec<MarbleTexture> CODEC = LosingMyMarblesRegistries.MARBLE_TEXTURE_TYPE.byNameCodec()
			.dispatch(MarbleTexture::type, MarbleTexture.Type::codec);
	StreamCodec<RegistryFriendlyByteBuf, MarbleTexture> STREAM_CODEC = ByteBufCodecs.registry(LosingMyMarblesRegistries.MARBLE_TEXTURE_TYPE.key())
			.dispatch(MarbleTexture::type, MarbleTexture.Type::streamCodec);

	ResourceLocation get(MarbleEntity entity);

	Type<?> type();

	record Type<T extends MarbleTexture>(MapCodec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
	}
}
