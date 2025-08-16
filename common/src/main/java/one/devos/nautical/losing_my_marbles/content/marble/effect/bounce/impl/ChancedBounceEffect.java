package one.devos.nautical.losing_my_marbles.content.marble.effect.bounce.impl;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.phys.Vec3;
import one.devos.nautical.losing_my_marbles.content.marble.MarbleEntity;
import one.devos.nautical.losing_my_marbles.content.marble.effect.bounce.BounceEffect;
import one.devos.nautical.losing_my_marbles.framework.data.LosingMyMarblesCodecs;

public record ChancedBounceEffect(BounceEffect effect, float chance) implements BounceEffect {
	public static final MapCodec<ChancedBounceEffect> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			BounceEffect.CODEC.fieldOf("effect").forGetter(ChancedBounceEffect::effect),
			LosingMyMarblesCodecs.OPEN_NORMALIZED_FLOAT.fieldOf("chance").forGetter(ChancedBounceEffect::chance)
	).apply(i, ChancedBounceEffect::new));

	@Override
	public void apply(MarbleEntity entity, Vec3 oldVel, Vec3 newVel) {
		if (entity.getRandom().nextFloat() < this.chance) {
			this.effect.apply(entity, oldVel, newVel);
		}
	}

	@Override
	public MapCodec<? extends BounceEffect> codec() {
		return CODEC;
	}
}
