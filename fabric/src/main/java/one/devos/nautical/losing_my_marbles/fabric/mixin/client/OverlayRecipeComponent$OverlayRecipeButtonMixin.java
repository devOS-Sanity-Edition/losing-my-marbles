package one.devos.nautical.losing_my_marbles.fabric.mixin.client;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.recipebook.OverlayRecipeComponent.OverlayRecipeButton.Pos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import one.devos.nautical.losing_my_marbles.framework.extension.OverlayCraftingRecipeButtonExt;

@Mixin(targets = "net.minecraft.client.gui.screens.recipebook.OverlayRecipeComponent$OverlayRecipeButton")
public abstract class OverlayRecipeComponent$OverlayRecipeButtonMixin extends AbstractWidget {
	@Shadow
	protected static Pos createGridPos(int x, int y, List<ItemStack> possibleItems) {
		throw new AbstractMethodError();
	}

	protected OverlayRecipeComponent$OverlayRecipeButtonMixin(int x, int y, int width, int height, Component message) {
		super(x, y, width, height, message);
	}

	@Inject(method = "renderWidget", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix3x2fStack;translate(FF)Lorg/joml/Matrix3x2f;", ordinal = 0))
	private void modifyIngredientSpacingForRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci, @Local Pos pos) {
		if (this instanceof OverlayCraftingRecipeButtonExt ext && ext.lmm$isMarbleMaker()) {
			graphics.pose().translate(pos.x() == 3 ? 2f : -2f, 0);
		}
	}
}
