package one.devos.nautical.losing_my_marbles.content;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import one.devos.nautical.losing_my_marbles.LosingMyMarbles;
import one.devos.nautical.losing_my_marbles.content.marble.recipe.MarbleRecipeDisplay;

public final class LosingMyMarblesRecipeDisplays {
	public static void init() {
		Registry.register(BuiltInRegistries.RECIPE_DISPLAY, LosingMyMarbles.id("marble"), MarbleRecipeDisplay.TYPE);
	}
}
