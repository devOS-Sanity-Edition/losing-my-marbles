package one.devos.nautical.losing_my_marbles.content.marble.effect.contact.entity.impl;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.minecraft.world.entity.Entity;
import one.devos.nautical.losing_my_marbles.content.marble.MarbleEntity;
import one.devos.nautical.losing_my_marbles.content.marble.effect.contact.entity.EntityContactEffect;

public record Compound(List<EntityContactEffect> effects) implements EntityContactEffect {
	public static final Codec<Compound> CODEC = EntityContactEffect.CODEC.listOf(2, Integer.MAX_VALUE).xmap(Compound::new, Compound::effects);
	public static final MapCodec<Compound> MAP_CODEC = CODEC.fieldOf("effects");

	@Override
	public void apply(MarbleEntity entity, Entity target) {
		for (EntityContactEffect effect : this.effects) {
			effect.apply(entity, target);
		}
	}

	@Override
	public MapCodec<? extends EntityContactEffect> codec() {
		return MAP_CODEC;
	}
}
