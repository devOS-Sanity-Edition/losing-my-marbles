package one.devos.nautical.losing_my_marbles.content;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeType;
import one.devos.nautical.losing_my_marbles.LosingMyMarbles;
import one.devos.nautical.losing_my_marbles.content.marble.recipe.MarbleRecipe;

public final class LosingMyMarblesRecipeTypes {
	public static final RecipeType<MarbleRecipe> MARBLE = Registry.register(
			BuiltInRegistries.RECIPE_TYPE,
			LosingMyMarbles.id("marble"),
			new RecipeType<MarbleRecipe>() {
				@Override
				public String toString() {
					return "losing_my_marbles:marble";
				}
			}
	);

	public static void init() {
	}
}
