package one.devos.nautical.losing_my_marbles.content.marble.asset.texture;

import java.util.function.Function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import one.devos.nautical.losing_my_marbles.LosingMyMarbles;

public final class MarbleTextures {
	private static final ExtraCodecs.LateBoundIdMapper<ResourceLocation, MapCodec<? extends MarbleTexture>> ID_MAPPER = new ExtraCodecs.LateBoundIdMapper<>();
	public static final Codec<MarbleTexture> CODEC = ID_MAPPER.codec(ResourceLocation.CODEC).dispatch(MarbleTexture::type, Function.identity());

	static {
		ID_MAPPER.put(LosingMyMarbles.id("stepped"), SteppedMarbleTexture.CODEC);
		ID_MAPPER.put(LosingMyMarbles.id("static"), StaticMarbleTexture.CODEC);
	}
}
