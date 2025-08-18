package one.devos.nautical.losing_my_marbles.fabric.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.recipebook.OverlayRecipeComponent.OverlayRecipeButton.Pos;
import net.minecraft.network.chat.Component;
import one.devos.nautical.losing_my_marbles.framework.extension.OverlayCraftingRecipeButtonExt;

@Mixin(targets = "net.minecraft.client.gui.screens.recipebook.OverlayRecipeComponent$OverlayRecipeButton")
public abstract class OverlayRecipeComponent$OverlayRecipeButtonMixin extends AbstractWidget {
	public OverlayRecipeComponent$OverlayRecipeButtonMixin(int i, int j, int k, int l, Component component) {
		super(i, j, k, l, component);
	}

	@Inject(method = "renderWidget", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix3x2fStack;translate(FF)Lorg/joml/Matrix3x2f;", ordinal = 0))
	private void lmm$modifyIngredientSpacingForRender(GuiGraphics $$0, int $$1, int $$2, float $$3, CallbackInfo ci, @Local Pos pos) {
		if (this instanceof OverlayCraftingRecipeButtonExt ext && ext.lmm$isMarbleMaker()) {
			$$0.pose().translate(pos.x() == 3 ? 2f : -2f, 0);
		}
	}
}
