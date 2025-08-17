package one.devos.nautical.losing_my_marbles.content.marble.maker;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.ItemCombinerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import one.devos.nautical.losing_my_marbles.LosingMyMarbles;

public final class MarbleMakerScreen extends ItemCombinerScreen<MarbleMakerMenu> {
	private static final ResourceLocation ERROR_SPRITE = LosingMyMarbles.id("marble_maker/error");
	private static final int ERROR_ICON_WIDTH = 28;
	private static final int ERROR_ICON_HEIGHT = 21;
	private static final int ERROR_ICON_X = 89;
	private static final int ERROR_ICON_Y = 33;

	public MarbleMakerScreen(MarbleMakerMenu menu, Inventory playerInventory, Component title) {
		super(menu, playerInventory, title, LosingMyMarbles.id("textures/gui/container/marble_maker.png"));
	}

	@Override
	protected void renderErrorIcon(GuiGraphics graphics, int x, int y) {
		if (this.menu.hasRecipeError()) {
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, ERROR_SPRITE, x + ERROR_ICON_X, y + ERROR_ICON_Y, ERROR_ICON_WIDTH, ERROR_ICON_HEIGHT);
		}
	}
}
