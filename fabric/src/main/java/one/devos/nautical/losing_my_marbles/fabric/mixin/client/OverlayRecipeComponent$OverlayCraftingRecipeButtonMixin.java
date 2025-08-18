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
import one.devos.nautical.losing_my_marbles.framework.extension.OverlayCraftingRecipeButtonExt;

@Mixin(targets = "net.minecraft.client.gui.screens.recipebook.OverlayRecipeComponent$OverlayCraftingRecipeButton")
public abstract class OverlayRecipeComponent$OverlayCraftingRecipeButtonMixin extends OverlayRecipeComponent$OverlayRecipeButtonMixin implements OverlayCraftingRecipeButtonExt {
	@Unique
	private static final ResourceLocation ENABLED_SPRITE = LosingMyMarbles.id("recipe_book/marble_maker_overlay");
	@Unique
	private static final ResourceLocation HIGHLIGHTED_ENABLED_SPRITE = LosingMyMarbles.id("recipe_book/marble_maker_overlay_highlighted");
	@Unique
	private static final ResourceLocation DISABLED_SPRITE = LosingMyMarbles.id("recipe_book/marble_maker_overlay_disabled");
	@Unique
	private static final ResourceLocation HIGHLIGHTED_DISABLED_SPRITE = LosingMyMarbles.id("recipe_book/marble_maker_overlay_disabled_highlighted");

	@Unique
	private boolean isMarbleMaker;

	protected OverlayRecipeComponent$OverlayCraftingRecipeButtonMixin(int x, int y, int width, int height, Component message) {
		super(x, y, width, height, message);
	}

	@Inject(method = "<init>", at = @At("TAIL"))
	private void saveInfoAboutIfThisIsMarbleMaker(OverlayRecipeComponent overlayRecipeComponent, int i, int j, RecipeDisplayId recipeDisplayId, RecipeDisplay recipeDisplay, ContextMap contextMap, boolean bl, CallbackInfo ci) {
		if (recipeDisplay instanceof MarbleRecipeDisplay) {
			this.isMarbleMaker = true;
		}
	}

	@Inject(method = "calculateIngredientsPositions", at = @At("HEAD"), cancellable = true)
	private static void modifyIngredientPositionsForMarbleMaker(RecipeDisplay recipeDisplay, ContextMap contextMap, CallbackInfoReturnable<List<Pos>> cir) {
		if (recipeDisplay instanceof MarbleRecipeDisplay marbleRecipeDisplay) {
			List<Pos> list = new ArrayList<>();

			List<SlotDisplay> slotDisplays = List.of(marbleRecipeDisplay.material(), marbleRecipeDisplay.addition());

			int i = 0;
			int[] positions = {0, 2};
			for (SlotDisplay slotDisplay : slotDisplays) {
				List<ItemStack> stacks = slotDisplay.resolveForStacks(contextMap);
				if (!stacks.isEmpty()) {
					list.add(createGridPos(positions[i], 1, stacks));
				}
				i++;
			}

			cir.setReturnValue(list);
		}
	}

	@Inject(method = "getSprite", at = @At("HEAD"), cancellable = true)
	private void changeTexturesForMarbleMaker(boolean enabled, CallbackInfoReturnable<ResourceLocation> cir) {
		if (this.isMarbleMaker) {
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
		return this.isMarbleMaker;
	}
}
