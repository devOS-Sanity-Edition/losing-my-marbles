package one.devos.nautical.losing_my_marbles.content.marble.effect.bounce.impl;

import com.mojang.serialization.MapCodec;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import one.devos.nautical.losing_my_marbles.content.marble.MarbleEntity;
import one.devos.nautical.losing_my_marbles.content.marble.effect.bounce.BounceEffect;

public record TeleportOwner(FloatProvider damage) implements BounceEffect {
	public static final FloatProvider DEFAULT = ConstantFloat.of(5);

	public static final MapCodec<TeleportOwner> CODEC = FloatProvider.codec(0, Float.MAX_VALUE)
			.optionalFieldOf("damage", DEFAULT)
			.xmap(TeleportOwner::new, TeleportOwner::damage);

	@Override
	public void apply(MarbleEntity entity, Vec3 oldVel, Vec3 newVel) {
		if (!(entity.level() instanceof ServerLevel level))
			return;

		float damage = this.damage.sample(entity.getRandom());
		if (damage <= 0)
			return;

		LivingEntity owner = entity.getOwner();
		if (owner == null)
			return;

		// largely copied from ThrownEnderPearl

		Vec3 pos = entity.oldPosition();
		if (owner instanceof ServerPlayer player) {
			if (player.connection.isAcceptingMessages()) {
				if (entity.isOnPortalCooldown()) {
					owner.setPortalCooldown();
				}

				ServerPlayer teleported = player.teleport(new TeleportTransition(
						level, pos, Vec3.ZERO, 0, 0, Relative.union(Relative.ROTATION, Relative.DELTA), TeleportTransition.DO_NOTHING
				));

				if (teleported != null) {
					teleported.resetFallDistance();
					teleported.resetCurrentImpulseContext();
					teleported.hurtServer(player.level(), entity.damageSources().enderPearl(), damage);
				}
			}
		} else {
			Entity teleported = owner.teleport(new TeleportTransition(
					level, pos, owner.getDeltaMovement(), owner.getYRot(), owner.getXRot(), TeleportTransition.DO_NOTHING
			));
			if (teleported != null) {
				teleported.resetFallDistance();
			}
		}
	}

	@Override
	public MapCodec<? extends BounceEffect> codec() {
		return CODEC;
	}
}
