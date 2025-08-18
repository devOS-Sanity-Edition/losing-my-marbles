package one.devos.nautical.losing_my_marbles.fabric.mixin.client;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.gui.screens.recipebook.OverlayRecipeComponent;
import net.minecraft.client.gui.screens.recipebook.OverlayRecipeComponent.OverlayRecipeButton.Pos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import one.devos.nautical.losing_my_marbles.LosingMyMarbles;
import one.devos.nautical.losing_my_marbles.content.marble.recipe.MarbleRecipeDisplay;
import one.devos.nautical.losing_my_marbles.fabric.mixin.accessors.OverlayRecipeComponent$OverlayRecipeButtonAccessor;
import one.devos.nautical.losing_my_marbles.framework.extension.OverlayCraftingRecipeButtonExt;

@Mixin(targets = "net.minecraft.client.gui.screens.recipebook.OverlayRecipeComponent$OverlayCraftingRecipeButton")
public abstract class OverlayRecipeComponent$OverlayCraftingRecipeButtonMixin extends OverlayRecipeComponent$OverlayRecipeButtonMixin implements OverlayCraftingRecipeButtonExt {
	@Unique private static final ResourceLocation ENABLED_SPRITE = LosingMyMarbles.id("recipe_book/marble_maker_overlay");
	@Unique private static final ResourceLocation HIGHLIGHTED_ENABLED_SPRITE = LosingMyMarbles.id("recipe_book/marble_maker_overlay_highlighted");
	@Unique private static final ResourceLocation DISABLED_SPRITE = LosingMyMarbles.id("recipe_book/marble_maker_overlay_disabled");
	@Unique private static final ResourceLocation HIGHLIGHTED_DISABLED_SPRITE = LosingMyMarbles.id("recipe_book/marble_maker_overlay_disabled_highlighted");

	@Unique private boolean lmm$isMarbleMaker;

	public OverlayRecipeComponent$OverlayCraftingRecipeButtonMixin(int i, int j, int k, int l, Component component) {
		super(i, j, k, l, component);
	}

	@Inject(method = "<init>", at = @At("TAIL"))
	private void lmm$saveInfoAboutIfThisIsMarbleMaker(OverlayRecipeComponent overlayRecipeComponent, int i, int j, RecipeDisplayId recipeDisplayId, RecipeDisplay recipeDisplay, ContextMap contextMap, boolean bl, CallbackInfo ci) {
		if (recipeDisplay instanceof MarbleRecipeDisplay) {
			lmm$isMarbleMaker = true;
		}
	}

	@Inject(method = "calculateIngredientsPositions", at = @At("HEAD"), cancellable = true)
	private static void lmm$modifyIngredientPositionsForMarbleMaker(RecipeDisplay recipeDisplay, ContextMap contextMap, CallbackInfoReturnable<List<Pos>> cir) {
		List<Pos> list = new ArrayList<>();

		if (recipeDisplay instanceof MarbleRecipeDisplay marbleRecipeDisplay) {
			List<SlotDisplay> slotDisplays = List.of(marbleRecipeDisplay.material(), marbleRecipeDisplay.addition());

			int i = 0;
			int[] positions = {0, 2};
			for (SlotDisplay slotDisplay : slotDisplays) {
				List<ItemStack> stacks = slotDisplay.resolveForStacks(contextMap);
				if (!stacks.isEmpty()) {
					list.add(OverlayRecipeComponent$OverlayRecipeButtonAccessor.lmm$createGridPos(positions[i], 1, stacks));
				}
				i++;
			}
		}

		cir.setReturnValue(list);
	}

	@Inject(method = "getSprite", at = @At("HEAD"), cancellable = true)
	private void lmm$changeTexturesForMarbleMaker(boolean enabled, CallbackInfoReturnable<ResourceLocation> cir) {
		if (lmm$isMarbleMaker) {
			ResourceLocation sprite;

			if (enabled) {
				sprite = this.isHoveredOrFocused() ? HIGHLIGHTED_ENABLED_SPRITE : ENABLED_SPRITE;
			} else {
				sprite = this.isHoveredOrFocused() ? HIGHLIGHTED_DISABLED_SPRITE : DISABLED_SPRITE;
			}

			cir.setReturnValue(sprite);
		}
	}

	@Override
	public boolean lmm$isMarbleMaker() {
		return lmm$isMarbleMaker;
	}
}
