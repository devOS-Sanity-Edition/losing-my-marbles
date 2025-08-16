package one.devos.nautical.losing_my_marbles.content.marble.effect.bounce.impl;

import java.util.List;

import com.mojang.serialization.MapCodec;

import net.minecraft.world.phys.Vec3;
import one.devos.nautical.losing_my_marbles.content.marble.MarbleEntity;
import one.devos.nautical.losing_my_marbles.content.marble.effect.bounce.BounceEffect;

public record Compound(List<BounceEffect> effects) implements BounceEffect {
	public static final MapCodec<Compound> CODEC = BounceEffect.CODEC
			.listOf(2, Integer.MAX_VALUE)
			.fieldOf("effects")
			.xmap(Compound::new, Compound::effects);

	@Override
	public void apply(MarbleEntity entity, Vec3 oldVel, Vec3 newVel) {
		for (BounceEffect effect : this.effects) {
			effect.apply(entity, oldVel, newVel);
		}
	}

	@Override
	public MapCodec<? extends BounceEffect> codec() {
		return CODEC;
	}
}
