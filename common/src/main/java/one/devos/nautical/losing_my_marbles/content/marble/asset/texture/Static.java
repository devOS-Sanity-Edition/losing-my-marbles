package one.devos.nautical.losing_my_marbles.content.marble.asset.texture;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;

public record Static(ResourceLocation assetId) implements MarbleTexture {
	public static final MapCodec<Static> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			ResourceLocation.CODEC.fieldOf("asset_id").forGetter(Static::assetId)
	).apply(i, Static::new));

	@Override
	public ResourceLocation get(Context context) {
		return this.assetId;
	}

	@Override
	public MapCodec<? extends MarbleTexture> type() {
		return CODEC;
	}
}
