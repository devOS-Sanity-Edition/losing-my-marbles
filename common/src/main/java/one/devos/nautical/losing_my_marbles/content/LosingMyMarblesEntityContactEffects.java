package one.devos.nautical.losing_my_marbles.content;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.Registry;
import one.devos.nautical.losing_my_marbles.LosingMyMarbles;
import one.devos.nautical.losing_my_marbles.content.marble.effect.contact.entity.EntityContactEffect;
import one.devos.nautical.losing_my_marbles.content.marble.effect.contact.entity.impl.Compound;
import one.devos.nautical.losing_my_marbles.content.marble.effect.contact.entity.impl.Damage;
import one.devos.nautical.losing_my_marbles.content.marble.effect.contact.entity.impl.Effect;
import one.devos.nautical.losing_my_marbles.content.marble.effect.contact.entity.impl.Ignite;

public final class LosingMyMarblesEntityContactEffects {
	public static void init() {
		register("compound", Compound.MAP_CODEC);
	    register("damage", Damage.CODEC);
		register("effect", Effect.CODEC);
		register("ignite", Ignite.CODEC);
	}

	private static void register(String name, MapCodec<? extends EntityContactEffect> codec) {
		Registry.register(LosingMyMarblesRegistries.ENTITY_CONTACT_EFFECT_TYPE, LosingMyMarbles.id(name), codec);
	}
}
