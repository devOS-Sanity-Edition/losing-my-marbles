package one.devos.nautical.losing_my_marbles.content.marble.effect.contact.entity.impl;

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

public record Damage(Holder<DamageType> type, FloatProvider amount) implements EntityContactEffect {
	public static final MapCodec<Damage> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			DamageType.CODEC.fieldOf("damage_type").forGetter(Damage::type),
			FloatProvider.codec(0, Float.MAX_VALUE).fieldOf("amount").forGetter(Damage::amount)
	).apply(i, Damage::new));

	@Override
	public void apply(MarbleEntity entity, Entity target) {
		if (!(entity.level() instanceof ServerLevel level))
			return;

		DamageSource source = new DamageSource(this.type, entity, entity.getOwner());
		float amount = this.amount.sample(entity.getRandom());

		target.hurtServer(level, source, amount);
	}

	@Override
	public MapCodec<? extends EntityContactEffect> codec() {
		return CODEC;
	}
}
