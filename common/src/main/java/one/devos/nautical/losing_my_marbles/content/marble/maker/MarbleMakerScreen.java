package one.devos.nautical.losing_my_marbles.content.marble.maker;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import one.devos.nautical.losing_my_marbles.LosingMyMarbles;
import one.devos.nautical.losing_my_marbles.content.marble.recipeBook.MarbleMakerRecipeBookComponent;

public final class MarbleMakerScreen extends AbstractRecipeBookScreen<MarbleMakerMenu> {
	private static final ResourceLocation ERROR_SPRITE = LosingMyMarbles.id("marble_maker/error");
	private static final int ERROR_ICON_WIDTH = 28;
	private static final int ERROR_ICON_HEIGHT = 21;
	private static final int ERROR_ICON_X = 89;
	private static final int ERROR_ICON_Y = 33;

	private final ResourceLocation menuResource = LosingMyMarbles.id("textures/gui/container/marble_maker.png");

	public MarbleMakerScreen(MarbleMakerMenu menu, Inventory playerInventory, Component title) {
		super(menu, new MarbleMakerRecipeBookComponent(menu), playerInventory, title);
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, this.menuResource, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
		if (this.menu.hasRecipeError()) {
			guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, ERROR_SPRITE, this.leftPos + ERROR_ICON_X, this.topPos + ERROR_ICON_Y, ERROR_ICON_WIDTH, ERROR_ICON_HEIGHT);
		}
	}

	@Override
	protected ScreenPosition getRecipeBookButtonPosition() {
		return new ScreenPosition(this.leftPos + 8, this.height / 2 - 49);
	}
}
