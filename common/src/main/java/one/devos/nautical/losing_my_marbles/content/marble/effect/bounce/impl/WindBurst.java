package one.devos.nautical.losing_my_marbles.content.marble.effect.bounce.impl;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.UniformFloat;
import net.minecraft.world.entity.projectile.windcharge.AbstractWindCharge;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import one.devos.nautical.losing_my_marbles.content.marble.MarbleEntity;
import one.devos.nautical.losing_my_marbles.content.marble.effect.bounce.BounceEffect;

public record WindBurst(FloatProvider radius) implements BounceEffect {
	public static final FloatProvider DEFAULT = UniformFloat.of(3, 5);

	public static final MapCodec<WindBurst> CODEC = FloatProvider.CODEC
			.optionalFieldOf("radius", DEFAULT)
			.xmap(WindBurst::new, WindBurst::radius);

	@Override
	public void apply(MarbleEntity entity, Vec3 oldVel, Vec3 newVel) {
		if (entity.level().isClientSide())
			return;

		// from WindChargedMobEffect
		Vec3 pos = entity.getBoundingBox().getCenter();
		float radius = this.radius.sample(entity.getRandom());

		entity.level().explode(
				entity, null, AbstractWindCharge.EXPLOSION_DAMAGE_CALCULATOR,
				pos.x, pos.y, pos.z, radius, false, Level.ExplosionInteraction.TRIGGER,
				ParticleTypes.GUST_EMITTER_SMALL, ParticleTypes.GUST_EMITTER_LARGE,
				SoundEvents.BREEZE_WIND_CHARGE_BURST
		);
	}

	@Override
	public MapCodec<? extends BounceEffect> codec() {
		return CODEC;
	}
}
