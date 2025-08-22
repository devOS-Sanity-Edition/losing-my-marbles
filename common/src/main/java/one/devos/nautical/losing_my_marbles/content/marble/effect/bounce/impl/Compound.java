package one.devos.nautical.losing_my_marbles.content.marble.effect.bounce.impl;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.minecraft.world.phys.Vec3;
import one.devos.nautical.losing_my_marbles.content.marble.MarbleEntity;
import one.devos.nautical.losing_my_marbles.content.marble.effect.bounce.BounceEffect;

public record Compound(List<BounceEffect> effects) implements BounceEffect {
	public static final Codec<Compound> CODEC = BounceEffect.CODEC.listOf(2, Integer.MAX_VALUE).xmap(Compound::new, Compound::effects);
	public static final MapCodec<Compound> MAP_CODEC = CODEC.fieldOf("effects");

	@Override
	public void apply(MarbleEntity entity, Vec3 oldVel, Vec3 newVel) {
		for (BounceEffect effect : this.effects) {
			effect.apply(entity, oldVel, newVel);
		}
	}

	@Override
	public void whenKilled(MarbleEntity entity) {
		for (BounceEffect effect : this.effects) {
			effect.whenKilled(entity);
		}
	}

	@Override
	public MapCodec<? extends BounceEffect> codec() {
		return MAP_CODEC;
	}
}
