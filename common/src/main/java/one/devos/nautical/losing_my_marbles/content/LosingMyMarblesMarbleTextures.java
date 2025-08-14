package one.devos.nautical.losing_my_marbles.content;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import one.devos.nautical.losing_my_marbles.LosingMyMarbles;
import one.devos.nautical.losing_my_marbles.content.marble.data.texture.MarbleTexture;
import one.devos.nautical.losing_my_marbles.content.marble.data.texture.StaticMarbleTexture;
import one.devos.nautical.losing_my_marbles.content.marble.data.texture.SteppedMarbleTexture;

public final class LosingMyMarblesMarbleTextures {
	public static void init() {
		register("stepped", SteppedMarbleTexture.TYPE);
	    register("static", StaticMarbleTexture.TYPE);
	}

	private static void register(String name, MarbleTexture.Type<?> type) {
		ResourceLocation id = LosingMyMarbles.id(name);
		Registry.register(LosingMyMarblesRegistries.MARBLE_TEXTURE_TYPE, id, type);
	}
}
