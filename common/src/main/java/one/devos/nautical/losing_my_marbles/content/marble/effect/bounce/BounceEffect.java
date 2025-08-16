package one.devos.nautical.losing_my_marbles.content.marble.effect.bounce;

import java.util.function.Function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;
import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesRegistries;
import one.devos.nautical.losing_my_marbles.content.marble.MarbleEntity;
import one.devos.nautical.losing_my_marbles.content.marble.effect.bounce.impl.Compound;

/**
 * An effect that is applied when a {@link MarbleEntity} bounces.
 */
public interface BounceEffect {
	Codec<BounceEffect> CODEC = Codec.withAlternative(
			LosingMyMarblesRegistries.BOUNCE_EFFECT_TYPE.byNameCodec().dispatch(BounceEffect::codec, Function.identity()),
			Codec.lazyInitialized(() -> Compound.CODEC)
	);
	// I am not writing 500 stream codecs right now
	StreamCodec<RegistryFriendlyByteBuf, BounceEffect> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

	void apply(MarbleEntity entity, Vec3 oldVel, Vec3 newVel);

	MapCodec<? extends BounceEffect> codec();
}
