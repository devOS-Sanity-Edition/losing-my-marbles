package one.devos.nautical.losing_my_marbles.content.marble.effect.contact.entity.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.minecraft.world.entity.Entity;
import one.devos.nautical.losing_my_marbles.content.marble.MarbleEntity;
import one.devos.nautical.losing_my_marbles.content.marble.effect.contact.entity.EntityContactEffect;

public record Ignite(int ticks) implements EntityContactEffect {
	public static final MapCodec<Ignite> CODEC = Codec.intRange(0, Integer.MAX_VALUE).fieldOf("ticks").xmap(Ignite::new, Ignite::ticks);

	@Override
	public void apply(MarbleEntity entity, Entity target) {
		if (entity.level().isClientSide())
			return;

		target.igniteForTicks(this.ticks);
	}

	@Override
	public MapCodec<? extends EntityContactEffect> codec() {
		return CODEC;
	}
}
