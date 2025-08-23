package one.devos.nautical.losing_my_marbles.content.marble.effect.bounce.impl;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.phys.Vec3;
import one.devos.nautical.losing_my_marbles.content.marble.MarbleEntity;
import one.devos.nautical.losing_my_marbles.content.marble.effect.bounce.BounceEffect;

public record Break(double threshold, Optional<BounceEffect> whenBroken, boolean triggerOnKill) implements BounceEffect {
	public static final MapCodec<Break> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Codec.doubleRange(0, Double.MAX_VALUE).optionalFieldOf("threshold", 0.5).forGetter(Break::threshold),
			BounceEffect.CODEC.optionalFieldOf("when_broken").forGetter(Break::whenBroken),
			Codec.BOOL.optionalFieldOf("trigger_on_kill", true).forGetter(Break::triggerOnKill)
	).apply(i, Break::new));

	@Override
	public void apply(MarbleEntity entity, Vec3 oldVel, Vec3 newVel) {
		double change = oldVel.vectorTo(newVel).length();
		if (change <= this.threshold)
			return;

		this.whenBroken.ifPresent(effect -> effect.apply(entity, oldVel, newVel));

		if (entity.level().isClientSide())
			return;

		entity.discard();
	}

	@Override
	public void whenKilled(MarbleEntity entity) {
		if (!this.triggerOnKill)
			return;

		// treat death as a bounce that caused a break
		this.whenBroken.ifPresent(effect -> effect.apply(entity, Vec3.ZERO, Vec3.ZERO));

		if (entity.level().isClientSide())
			return;

		entity.discard();
	}

	@Override
	public MapCodec<? extends BounceEffect> codec() {
		return CODEC;
	}
}
