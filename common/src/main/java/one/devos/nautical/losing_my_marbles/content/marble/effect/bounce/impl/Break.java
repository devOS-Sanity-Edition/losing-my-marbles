package one.devos.nautical.losing_my_marbles.content.marble.effect.bounce.impl;

import java.util.Optional;

import com.mojang.serialization.MapCodec;

import net.minecraft.world.phys.Vec3;
import one.devos.nautical.losing_my_marbles.content.marble.MarbleEntity;
import one.devos.nautical.losing_my_marbles.content.marble.effect.bounce.BounceEffect;

public record Break(Optional<BounceEffect> whenBroken) implements BounceEffect {
	public static final MapCodec<Break> CODEC = BounceEffect.CODEC.optionalFieldOf("when_broken").xmap(Break::new, Break::whenBroken);

	@Override
	public void apply(MarbleEntity entity, Vec3 oldVel, Vec3 newVel) {
		entity.discard();

		this.whenBroken.ifPresent(effect -> effect.apply(entity, oldVel, newVel));
	}

	@Override
	public MapCodec<? extends BounceEffect> codec() {
		return CODEC;
	}
}
