package one.devos.nautical.losing_my_marbles.content.marble.effect.contact.entity.impl;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import one.devos.nautical.losing_my_marbles.content.marble.MarbleEntity;
import one.devos.nautical.losing_my_marbles.content.marble.effect.contact.entity.EntityContactEffect;
import one.devos.nautical.losing_my_marbles.framework.data.LosingMyMarblesCodecs;

public record Damage(Holder<DamageType> type, FloatProvider amount, boolean velocityScaled, Optional<Float> minimum) implements EntityContactEffect {
	public static final MapCodec<Damage> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			DamageType.CODEC.fieldOf("damage_type").forGetter(Damage::type),
			FloatProvider.codec(0, Float.MAX_VALUE).fieldOf("amount").forGetter(Damage::amount),
			Codec.BOOL.optionalFieldOf("velocity_scaled", false).forGetter(Damage::velocityScaled),
			LosingMyMarblesCodecs.POSITIVE_FLOAT.optionalFieldOf("minimum").forGetter(Damage::minimum)
	).apply(i, Damage::new));

	@Override
	public void apply(MarbleEntity entity, Entity target) {
		if (!(entity.level() instanceof ServerLevel level))
			return;

		DamageSource source = new DamageSource(this.type, entity, entity.getOwner());
		float amount = this.amount.sample(entity.getRandom());

		if (this.velocityScaled) {
			amount *= (float) entity.getDeltaMovement().length();
		}

		if (this.minimum.isPresent() && amount < this.minimum.get())
			return;

		target.hurtServer(level, source, amount);
	}

	@Override
	public MapCodec<? extends EntityContactEffect> codec() {
		return CODEC;
	}
}
