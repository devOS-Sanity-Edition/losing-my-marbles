package one.devos.nautical.losing_my_marbles.content.marble.asset.texture;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import one.devos.nautical.losing_my_marbles.framework.data.LosingMyMarblesCodecs;

public record SteppedMarbleTexture(float distancePerStep, List<ResourceLocation> assets) implements MarbleTexture {
	public static final MapCodec<SteppedMarbleTexture> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			LosingMyMarblesCodecs.POSITIVE_FLOAT.fieldOf("distance_per_step").forGetter(SteppedMarbleTexture::distancePerStep),
			Codec.list(ResourceLocation.CODEC, 2, Integer.MAX_VALUE).fieldOf("assets").forGetter(SteppedMarbleTexture::assets)
	).apply(i, SteppedMarbleTexture::new));

	@Override
	public ResourceLocation get(Context context) {
		boolean forward = Mth.sign(context.relativeViewPosition().dot(context.deltaMovement())) >= 0;
		int steps = this.assets.size();
		int step = Mth.floor(context.distanceTraveled() / this.distancePerStep) % steps;
		if (!forward)
			step = (steps - 1) - step;
		return this.assets.get(step);
	}

	@Override
	public MapCodec<? extends MarbleTexture> type() {
		return CODEC;
	}
}
