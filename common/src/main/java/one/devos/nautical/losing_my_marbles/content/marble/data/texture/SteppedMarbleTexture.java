package one.devos.nautical.losing_my_marbles.content.marble.data.texture;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import one.devos.nautical.losing_my_marbles.content.marble.MarbleEntity;
import one.devos.nautical.losing_my_marbles.framework.data.LosingMyMarblesCodecs;
import one.devos.nautical.losing_my_marbles.framework.network.LosingMyMarblesStreamCodecs;

public record SteppedMarbleTexture(float distancePerStep, List<ResourceLocation> textures) implements MarbleTexture {
	public static final MapCodec<SteppedMarbleTexture> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			LosingMyMarblesCodecs.POSITIVE_FLOAT.fieldOf("distance_per_step").forGetter(SteppedMarbleTexture::distancePerStep),
			Codec.list(ResourceLocation.CODEC, 2, Integer.MAX_VALUE).fieldOf("textures").forGetter(SteppedMarbleTexture::textures)
	).apply(i, SteppedMarbleTexture::new));

	public static final StreamCodec<ByteBuf, SteppedMarbleTexture> STREAM_CODEC = StreamCodec.composite(
			LosingMyMarblesStreamCodecs.POSITIVE_FLOAT, SteppedMarbleTexture::distancePerStep,
			LosingMyMarblesStreamCodecs.list(ResourceLocation.STREAM_CODEC, 2, Integer.MAX_VALUE), SteppedMarbleTexture::textures,
			SteppedMarbleTexture::new
	);

	public static final Type<SteppedMarbleTexture> TYPE = new Type<>(CODEC, STREAM_CODEC);

	@Override
	public ResourceLocation get(MarbleEntity entity) {
		throw new RuntimeException("TODO");
	}

	@Override
	public Type<?> type() {
		return TYPE;
	}
}
