package one.devos.nautical.losing_my_marbles.fabric.mixin.accessors;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.gui.screens.recipebook.OverlayRecipeComponent.OverlayRecipeButton.Pos;
import net.minecraft.world.item.ItemStack;

@Mixin(targets = "net.minecraft.client.gui.screens.recipebook.OverlayRecipeComponent$OverlayRecipeButton")
public interface OverlayRecipeComponent$OverlayRecipeButtonAccessor {
	@Invoker("createGridPos")
	static Pos lmm$createGridPos(int x, int y, List<ItemStack> list) {
		throw new AssertionError();
	}
}
