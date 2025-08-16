package one.devos.nautical.losing_my_marbles.content.marble.effect.bounce.impl;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.phys.Vec3;
import one.devos.nautical.losing_my_marbles.content.marble.MarbleEntity;
import one.devos.nautical.losing_my_marbles.content.marble.data.MarbleInstance;
import one.devos.nautical.losing_my_marbles.content.marble.effect.bounce.BounceEffect;

public record PatchComponents(DataComponentPatch patch) implements BounceEffect {
	public static final MapCodec<PatchComponents> CODEC = DataComponentPatch.CODEC.fieldOf("patch").xmap(PatchComponents::new, PatchComponents::patch);

	@Override
	public void apply(MarbleEntity entity, Vec3 oldVel, Vec3 newVel) {
		MarbleInstance marble = entity.marble().copy();
		marble.components().applyPatch(this.patch);
		entity.setMarble(marble);
	}

	@Override
	public MapCodec<? extends BounceEffect> codec() {
		return CODEC;
	}
}
