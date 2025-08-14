package one.devos.nautical.losing_my_marbles.content.marble.data.texture;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import one.devos.nautical.losing_my_marbles.content.marble.MarbleEntity;

public record StaticMarbleTexture(ResourceLocation id) implements MarbleTexture {
	public static final MapCodec<StaticMarbleTexture> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			ResourceLocation.CODEC.fieldOf("id").forGetter(StaticMarbleTexture::id)
	).apply(i, StaticMarbleTexture::new));

	public static final StreamCodec<ByteBuf, StaticMarbleTexture> STREAM_CODEC = StreamCodec.composite(
			ResourceLocation.STREAM_CODEC, StaticMarbleTexture::id,
			StaticMarbleTexture::new
	);

	public static final Type<StaticMarbleTexture> TYPE = new Type<>(CODEC, STREAM_CODEC);

	@Override
	public ResourceLocation get(MarbleEntity entity) {
		return this.id;
	}

	@Override
	public Type<?> type() {
		return TYPE;
	}
}
