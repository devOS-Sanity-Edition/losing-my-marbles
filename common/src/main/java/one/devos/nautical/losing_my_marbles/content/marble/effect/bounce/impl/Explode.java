package one.devos.nautical.losing_my_marbles.content.marble.effect.bounce.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.EntityBasedExplosionDamageCalculator;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.phys.Vec3;
import one.devos.nautical.losing_my_marbles.content.marble.MarbleEntity;
import one.devos.nautical.losing_my_marbles.content.marble.effect.bounce.BounceEffect;

public record Explode(FloatProvider radius, boolean fire, ExplosionInteraction interaction,
					  ParticleOptions smallParticles, ParticleOptions largeParticles, Holder<SoundEvent> sound) implements BounceEffect {
	public static final MapCodec<Explode> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			FloatProvider.CODEC.fieldOf("radius").forGetter(Explode::radius),
			Codec.BOOL.fieldOf("fire").forGetter(Explode::fire),
			ExplosionInteraction.CODEC.fieldOf("interaction").forGetter(Explode::interaction),
			ParticleTypes.CODEC.fieldOf("small_particles").forGetter(Explode::smallParticles),
			ParticleTypes.CODEC.fieldOf("large_particles").forGetter(Explode::largeParticles),
			SoundEvent.CODEC.fieldOf("sound").forGetter(Explode::sound)
	).apply(i, Explode::new));

	@Override
	public void apply(MarbleEntity entity, Vec3 oldVel, Vec3 newVel) {
		Level level = entity.level();
		if (level.isClientSide())
			return;

		// TODO: replace null with owner
		DamageSource source = level.damageSources().explosion(entity, null);
		ExplosionDamageCalculator calculator = new EntityBasedExplosionDamageCalculator(entity);
		Vec3 pos = entity.getBoundingBox().getCenter();

		level.explode(
				entity, source, calculator, pos.x, pos.y, pos.z,
				this.radius.sample(entity.getRandom()),
				this.fire, this.interaction, this.smallParticles, this.largeParticles, this.sound
		);
	}

	@Override
	public MapCodec<? extends BounceEffect> codec() {
		return CODEC;
	}
}
