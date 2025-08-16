package one.devos.nautical.losing_my_marbles.content.marble.effect.bounce.impl;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;

import net.minecraft.util.random.WeightedList;
import net.minecraft.world.phys.Vec3;
import one.devos.nautical.losing_my_marbles.content.marble.MarbleEntity;
import one.devos.nautical.losing_my_marbles.content.marble.effect.bounce.BounceEffect;

public record SelectWeighted(WeightedList<BounceEffect> effects) implements BounceEffect {
	public static final MapCodec<SelectWeighted> CODEC = WeightedList.nonEmptyCodec(BounceEffect.CODEC).validate(
			list -> list.unwrap().size() < 2 ? DataResult.error(() -> "List must contain at least 2 entries") : DataResult.success(list)
	).fieldOf("effects").xmap(SelectWeighted::new, SelectWeighted::effects);

	@Override
	public void apply(MarbleEntity entity, Vec3 oldVel, Vec3 newVel) {
		this.effects.getRandomOrThrow(entity.getRandom()).apply(entity, oldVel, newVel);
	}

	@Override
	public MapCodec<? extends BounceEffect> codec() {
		return CODEC;
	}
}
