package one.devos.nautical.losing_my_marbles.content.marble.asset;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import one.devos.nautical.losing_my_marbles.LosingMyMarbles;
import one.devos.nautical.losing_my_marbles.content.marble.asset.texture.MarbleTexture;
import one.devos.nautical.losing_my_marbles.content.marble.asset.texture.MarbleTextures;
import one.devos.nautical.losing_my_marbles.framework.data.LosingMyMarblesCodecs;

public record MarbleAsset(float scale, MarbleTexture texture) {
	public static final Codec<MarbleAsset> CODEC = RecordCodecBuilder.create(i -> i.group(
			LosingMyMarblesCodecs.POSITIVE_FLOAT.optionalFieldOf("scale", 1f).forGetter(MarbleAsset::scale),
			MarbleTextures.CODEC.fieldOf("texture").forGetter(MarbleAsset::texture)
	).apply(i, MarbleAsset::new));
	public static final ResourceKey<Registry<MarbleAsset>> REGISTRY_KEY = ResourceKey.createRegistryKey(LosingMyMarbles.id("marble_asset"));
}
