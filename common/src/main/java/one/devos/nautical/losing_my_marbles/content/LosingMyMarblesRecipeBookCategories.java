package one.devos.nautical.losing_my_marbles.content;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import one.devos.nautical.losing_my_marbles.LosingMyMarbles;

public class LosingMyMarblesRecipeBookCategories {
	public static final RecipeBookCategory MARBLE_MAKER = register("marble_maker");

	static RecipeBookCategory register(String name) {
		ResourceLocation id = LosingMyMarbles.id(name);
		return Registry.register(BuiltInRegistries.RECIPE_BOOK_CATEGORY, id, new RecipeBookCategory());
	}

	public static void init() {
	}
}
