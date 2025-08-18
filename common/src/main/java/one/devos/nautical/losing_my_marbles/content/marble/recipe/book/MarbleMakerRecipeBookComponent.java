package one.devos.nautical.losing_my_marbles.content.marble.recipe.book;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.recipebook.GhostSlots;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.gui.screens.recipebook.SearchRecipeBookCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import one.devos.nautical.losing_my_marbles.LosingMyMarbles;
import one.devos.nautical.losing_my_marbles.content.marble.maker.MarbleMakerMenu;
import one.devos.nautical.losing_my_marbles.content.marble.recipe.MarbleRecipeDisplay;
import one.devos.nautical.losing_my_marbles.framework.platform.PlatformClientHelper;

public class MarbleMakerRecipeBookComponent extends RecipeBookComponent<MarbleMakerMenu> {
	private static final WidgetSprites FILTER_BUTTON_SPRITES = new WidgetSprites(
			LosingMyMarbles.id("recipe_book/marble_maker_filter_enabled"),
			LosingMyMarbles.id("recipe_book/marble_maker_filter_disabled"),
			LosingMyMarbles.id("recipe_book/marble_maker_filter_enabled_highlighted"),
			LosingMyMarbles.id("recipe_book/marble_maker_filter_disabled_highlighted")
	);
	private static final Component ONLY_MARBLABLES = Component.translatable("gui.losing_my_marbles.recipebook.toggleRecipes.marble_maker");
	private static final List<TabInfo> TABS = List.of(new RecipeBookComponent.TabInfo(SearchRecipeBookCategory.valueOf("MARBLE_MAKER")));

	public MarbleMakerRecipeBookComponent(MarbleMakerMenu menu) {
		super(menu, TABS);
	}

	@Override
	protected void initFilterButtonTextures() {
		this.filterButton.initTextureValues(FILTER_BUTTON_SPRITES);
	}

	@Override
	protected boolean isCraftingSlot(Slot slot) {
		return switch (slot.index) {
			case 0, 1, 2 -> true;
			default -> false;
		};
	}

	@Override
	protected void selectMatchingRecipes(RecipeCollection recipeCollection, StackedItemContents stackedItemContents) {
		recipeCollection.selectRecipes(stackedItemContents, i -> i instanceof MarbleRecipeDisplay);
	}

	@NotNull
	@Override
	protected Component getRecipeFilterName() {
		return ONLY_MARBLABLES;
	}

	@Override
	protected void fillGhostRecipe(GhostSlots ghostSlots, RecipeDisplay recipeDisplay, ContextMap contextMap) {
		PlatformClientHelper.INSTANCE.setGhostSlotsResult(ghostSlots, this.menu.getSlot(this.menu.getResultSlot()), contextMap, recipeDisplay.result());
		if (recipeDisplay instanceof MarbleRecipeDisplay marbleRecipeDisplay) {
			PlatformClientHelper.INSTANCE.setGhostSlotsInput(ghostSlots, this.menu.getSlot(0), contextMap, marbleRecipeDisplay.material());
			Slot slot = this.menu.getSlot(1);
			if (!slot.hasItem()) {
				PlatformClientHelper.INSTANCE.setGhostSlotsInput(ghostSlots, slot, contextMap, marbleRecipeDisplay.addition());
			}
		}
	}
}
