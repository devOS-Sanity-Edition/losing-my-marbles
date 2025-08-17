package one.devos.nautical.losing_my_marbles.fabric.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipePropertySet;
import one.devos.nautical.losing_my_marbles.framework.data.RecipePropertySetRegistry;

@Mixin(RecipeManager.class)
public class RecipeManagerMixin {
	@SuppressWarnings("NonConstantFieldWithUpperCaseName")
	@Shadow
	@Final
	@Mutable
	private static Map<ResourceKey<RecipePropertySet>, RecipeManager.IngredientExtractor> RECIPE_PROPERTY_SETS;

	@Inject(method = "<clinit>", at = @At("TAIL"))
	private static void injectCustomRecipePropertySets(CallbackInfo ci) {
		RECIPE_PROPERTY_SETS = RecipePropertySetRegistry.inject(RECIPE_PROPERTY_SETS);
	}
}
