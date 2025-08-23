package one.devos.nautical.losing_my_marbles.content.marble.effect.bounce.impl;

import java.util.Optional;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.phys.Vec3;
import one.devos.nautical.losing_my_marbles.content.marble.MarbleEntity;
import one.devos.nautical.losing_my_marbles.content.marble.effect.bounce.BounceEffect;
import one.devos.nautical.losing_my_marbles.framework.data.LosingMyMarblesCodecs;

public record Chanced(BounceEffect effect, float chance, Optional<Float> chanceOnKill) implements BounceEffect {
	public static final MapCodec<Chanced> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			BounceEffect.CODEC.fieldOf("effect").forGetter(Chanced::effect),
			LosingMyMarblesCodecs.OPEN_NORMALIZED_FLOAT.fieldOf("chance").forGetter(Chanced::chance),
			LosingMyMarblesCodecs.NORMALIZED_FLOAT.optionalFieldOf("chance_on_kill").forGetter(Chanced::chanceOnKill)
	).apply(i, Chanced::new));

	@Override
	public void apply(MarbleEntity entity, Vec3 oldVel, Vec3 newVel) {
		if (entity.getRandom().nextFloat() < this.chance) {
			this.effect.apply(entity, oldVel, newVel);
		}
	}

	@Override
	public void whenKilled(MarbleEntity entity) {
		if (this.chanceOnKill.isEmpty())
			return;

		float chance = this.chanceOnKill.get();
		if (chance == 0)
			return;

		if (chance == 1 || entity.getRandom().nextFloat() < chance) {
			this.effect.whenKilled(entity);
		}
	}

	@Override
	public MapCodec<? extends BounceEffect> codec() {
		return CODEC;
	}
}
