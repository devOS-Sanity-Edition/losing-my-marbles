package one.devos.nautical.losing_my_marbles.content;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import one.devos.nautical.losing_my_marbles.LosingMyMarbles;
import one.devos.nautical.losing_my_marbles.content.marble.data.shape.CubeMarbleShape;
import one.devos.nautical.losing_my_marbles.content.marble.data.shape.MarbleShape;
import one.devos.nautical.losing_my_marbles.content.marble.data.shape.SphereMarbleShape;

public final class LosingMyMarblesMarbleShapeTypes {
	public static void init() {
	    register("sphere", SphereMarbleShape.TYPE);
		register("cube", CubeMarbleShape.TYPE);
	}

	private static void register(String name, MarbleShape.Type<?> type) {
		ResourceLocation id = LosingMyMarbles.id(name);
		Registry.register(LosingMyMarblesRegistries.MARBLE_SHAPE_TYPE, id, type);
	}
}
