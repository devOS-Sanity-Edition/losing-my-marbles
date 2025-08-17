package one.devos.nautical.losing_my_marbles.content;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.Registry;
import one.devos.nautical.losing_my_marbles.LosingMyMarbles;
import one.devos.nautical.losing_my_marbles.content.marble.effect.contact.block.BlockContactEffect;
import one.devos.nautical.losing_my_marbles.content.marble.effect.contact.block.impl.Compound;
import one.devos.nautical.losing_my_marbles.content.marble.effect.contact.block.impl.SetProperties;
import one.devos.nautical.losing_my_marbles.content.marble.effect.contact.block.impl.Swap;

public final class LosingMyMarblesBlockContactEffects {
	public static void init() {
	    register("compound", Compound.MAP_CODEC);
		register("set_properties", SetProperties.CODEC);
		register("swap", Swap.CODEC);
	}

	private static void register(String name, MapCodec<? extends BlockContactEffect> codec) {
		Registry.register(LosingMyMarblesRegistries.BLOCK_CONTACT_EFFECT_TYPE, LosingMyMarbles.id(name), codec);
	}
}
