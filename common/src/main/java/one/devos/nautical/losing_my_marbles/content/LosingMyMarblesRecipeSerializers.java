package one.devos.nautical.losing_my_marbles.content;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import one.devos.nautical.losing_my_marbles.LosingMyMarbles;
import one.devos.nautical.losing_my_marbles.content.marble.recipe.MarbleRecipe;

public final class LosingMyMarblesRecipeSerializers {
	public static final RecipeSerializer<MarbleRecipe> MARBLE = Registry.register(
			BuiltInRegistries.RECIPE_SERIALIZER,
			LosingMyMarbles.id("marble"),
			MarbleRecipe.Serializer.INSTANCE
	);

	public static void init() {
	}
}
