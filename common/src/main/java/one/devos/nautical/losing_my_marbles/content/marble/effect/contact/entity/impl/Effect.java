package one.devos.nautical.losing_my_marbles.content.marble.effect.contact.entity.impl;

import java.util.List;

import com.mojang.serialization.MapCodec;

import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import one.devos.nautical.losing_my_marbles.content.marble.MarbleEntity;
import one.devos.nautical.losing_my_marbles.content.marble.effect.contact.entity.EntityContactEffect;

public record Effect(List<MobEffectInstance> effects) implements EntityContactEffect {
	public static final MapCodec<Effect> CODEC = ExtraCodecs.nonEmptyList(MobEffectInstance.CODEC.listOf()).fieldOf("effects").xmap(Effect::new, Effect::effects);

	@Override
	public void apply(MarbleEntity entity, Entity target) {
		if (entity.level().isClientSide() || !(target instanceof LivingEntity living))
			return;

		for (MobEffectInstance effect : this.effects) {
			// copy, these are mutable
			living.addEffect(new MobEffectInstance(effect));
		}
	}

	@Override
	public MapCodec<? extends EntityContactEffect> codec() {
		return CODEC;
	}
}
