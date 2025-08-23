package one.devos.nautical.losing_my_marbles.content.marble.effect.bounce.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.random.WeightedList;
import net.minecraft.world.phys.Vec3;
import one.devos.nautical.losing_my_marbles.content.marble.MarbleEntity;
import one.devos.nautical.losing_my_marbles.content.marble.effect.bounce.BounceEffect;

public record SelectWeighted(WeightedList<BounceEffect> effects, boolean applyOnKill) implements BounceEffect {
	public static final Codec<WeightedList<BounceEffect>> EFFECTS_CODEC = WeightedList.nonEmptyCodec(BounceEffect.CODEC).validate(
			list -> list.unwrap().size() < 2 ? DataResult.error(() -> "List must contain at least 2 entries") : DataResult.success(list)
	);

	public static final MapCodec<SelectWeighted> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			EFFECTS_CODEC.fieldOf("effects").forGetter(SelectWeighted::effects),
			Codec.BOOL.optionalFieldOf("apply_on_kill", false).forGetter(SelectWeighted::applyOnKill)
	).apply(i, SelectWeighted::new));

	@Override
	public void apply(MarbleEntity entity, Vec3 oldVel, Vec3 newVel) {
		this.effects.getRandomOrThrow(entity.getRandom()).apply(entity, oldVel, newVel);
	}

	@Override
	public void whenKilled(MarbleEntity entity) {
		if (!this.applyOnKill)
			return;

		this.effects.getRandomOrThrow(entity.getRandom()).whenKilled(entity);
	}

	@Override
	public MapCodec<? extends BounceEffect> codec() {
		return CODEC;
	}
}
