package one.devos.nautical.losing_my_marbles.content.marble.effect.contact.entity;

import java.util.function.Function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesRegistries;
import one.devos.nautical.losing_my_marbles.content.marble.MarbleEntity;
import one.devos.nautical.losing_my_marbles.content.marble.effect.contact.entity.impl.Compound;

/**
 * An effect that a marble applies to all entities that it touches every tick.
 */
public interface EntityContactEffect {
	Codec<EntityContactEffect> CODEC = Codec.withAlternative(
			LosingMyMarblesRegistries.ENTITY_CONTACT_EFFECT_TYPE.byNameCodec().dispatch(EntityContactEffect::codec, Function.identity()),
			Codec.lazyInitialized(() -> Compound.CODEC)
	);
	StreamCodec<RegistryFriendlyByteBuf, EntityContactEffect> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

	void apply(MarbleEntity entity, Entity target);

	MapCodec<? extends EntityContactEffect> codec();
}
