package one.devos.nautical.losing_my_marbles.content;

import java.util.Optional;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipePropertySet;
import one.devos.nautical.losing_my_marbles.LosingMyMarbles;
import one.devos.nautical.losing_my_marbles.content.marble.recipe.MarbleRecipe;
import one.devos.nautical.losing_my_marbles.framework.data.RecipePropertySetRegistry;

public final class LosingMyMarblesRecipePropertySets {
	public static final ResourceKey<RecipePropertySet> MARBLE_MATERIAL = register(
			"marble_material",
			recipe -> recipe instanceof MarbleRecipe marbleRecipe ? Optional.of(marbleRecipe.material) : Optional.empty()
	);
	public static final ResourceKey<RecipePropertySet> MARBLE_ADDITION = register(
			"marble_addition",
			recipe -> recipe instanceof MarbleRecipe marbleRecipe ? marbleRecipe.addition : Optional.empty()
	);

	private static ResourceKey<RecipePropertySet> register(String name, RecipeManager.IngredientExtractor extractor) {
		return RecipePropertySetRegistry.register(LosingMyMarbles.id(name), extractor);
	}

	public static void init() {
	}
}
