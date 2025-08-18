package one.devos.nautical.losing_my_marbles.fabric.mixin.client;

import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.gui.screens.recipebook.SearchRecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesRecipeBookCategories;

@Mixin(SearchRecipeBookCategory.class)
public class SearchRecipeBookCategoryMixin {
	@SuppressWarnings("NonConstantFieldWithUpperCaseName")
	@Shadow
	@Final
	@Mutable
	private static SearchRecipeBookCategory[] $VALUES;

	@SuppressWarnings("SameParameterValue")
	@Invoker("<init>")
	private static SearchRecipeBookCategory create(String name, int ordinal, RecipeBookCategory... recipeBookCategories) {
		throw new AssertionError();
	}

	static {
		var entry = create("MARBLE_MAKER", $VALUES.length, LosingMyMarblesRecipeBookCategories.MARBLE_MAKER);

		$VALUES = ArrayUtils.addAll($VALUES, entry);
	}
}
