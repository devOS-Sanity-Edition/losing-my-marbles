package one.devos.nautical.losing_my_marbles.content;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.Registry;
import one.devos.nautical.losing_my_marbles.LosingMyMarbles;
import one.devos.nautical.losing_my_marbles.content.marble.effect.bounce.BounceEffect;
import one.devos.nautical.losing_my_marbles.content.marble.effect.bounce.impl.Break;
import one.devos.nautical.losing_my_marbles.content.marble.effect.bounce.impl.Chanced;
import one.devos.nautical.losing_my_marbles.content.marble.effect.bounce.impl.Compound;
import one.devos.nautical.losing_my_marbles.content.marble.effect.bounce.impl.Explode;
import one.devos.nautical.losing_my_marbles.content.marble.effect.bounce.impl.PlaceBlock;
import one.devos.nautical.losing_my_marbles.content.marble.effect.bounce.impl.PlaySound;
import one.devos.nautical.losing_my_marbles.content.marble.effect.bounce.impl.SpawnEntity;
import one.devos.nautical.losing_my_marbles.content.marble.effect.bounce.impl.TeleportOwner;
import one.devos.nautical.losing_my_marbles.content.marble.effect.bounce.impl.WindBurst;

public final class LosingMyMarblesBounceEffects {
	public static void init() {
		register("break", Break.CODEC);
		register("chanced", Chanced.CODEC);
		register("compound", Compound.CODEC);
		register("explode", Explode.CODEC);
		register("place_block", PlaceBlock.CODEC);
		register("play_sound", PlaySound.CODEC);
		register("spawn_entity", SpawnEntity.CODEC);
		register("teleport_owner", TeleportOwner.CODEC);
		register("wind_burst", WindBurst.CODEC);
	}

	private static void register(String name, MapCodec<? extends BounceEffect> codec) {
		Registry.register(LosingMyMarblesRegistries.BOUNCE_EFFECT_TYPE, LosingMyMarbles.id(name), codec);
	}
}
