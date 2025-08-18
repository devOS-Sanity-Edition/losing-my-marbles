package one.devos.nautical.losing_my_marbles.content.marble.maker;

import java.util.List;
import java.util.Optional;

import net.minecraft.recipebook.ServerPlaceRecipe;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeAccess;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipePropertySet;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesBlocks;
import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesMenus;
import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesRecipePropertySets;
import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesRecipeTypes;
import one.devos.nautical.losing_my_marbles.content.marble.recipe.MarbleRecipe;
import one.devos.nautical.losing_my_marbles.content.marble.recipe.MarbleRecipeInput;
import one.devos.nautical.losing_my_marbles.content.marble.recipeBook.MarbleMakerRecipeBookHelper;

import org.jetbrains.annotations.NotNull;

public final class MarbleMakerMenu extends RecipeBookMenu {
	private final Level level;
	private final DataSlot hasRecipeError = DataSlot.standalone();

	// Recipe book stuff
	private boolean placingRecipe;

	// Copied from ItemCombinerMenu
	private final ContainerLevelAccess access;
	private final Container inputSlots;
	private final ResultContainer resultSlots = new ResultContainer() {
		@Override
		public void setChanged() {
			MarbleMakerMenu.this.slotsChanged(this);
		}
	};
	private final int resultSlotIndex;

	public MarbleMakerMenu(int containerId, Inventory inventory, ContainerLevelAccess access, Level level) {
		super(LosingMyMarblesMenus.MARBLE_MAKER, containerId);

		this.level = level;
		this.addDataSlot(this.hasRecipeError).set(0);

		// Copied from ItemCombinerMenu
		ItemCombinerMenuSlotDefinition def = createInputSlotDefinitions(level.recipeAccess());
		this.access = access;
		this.inputSlots = this.createContainer(def.getNumOfInputSlots());
		this.resultSlotIndex = def.getResultSlotIndex();
		this.createInputSlots(def);
		this.createResultSlot(def);
		this.addStandardInventorySlots(inventory, 8, 84);
	}

	public MarbleMakerMenu(int containerId, Inventory inventory, ContainerLevelAccess access) {
		this(containerId, inventory, access, inventory.player.level());
	}

	public MarbleMakerMenu(int containerId, Inventory inventory) {
		this(containerId, inventory, ContainerLevelAccess.NULL);
	}

	private static ItemCombinerMenuSlotDefinition createInputSlotDefinitions(RecipeAccess recipeAccess) {
		RecipePropertySet materialItemTest = recipeAccess.propertySet(LosingMyMarblesRecipePropertySets.MARBLE_MATERIAL);
		RecipePropertySet additionItemTest = recipeAccess.propertySet(LosingMyMarblesRecipePropertySets.MARBLE_ADDITION);
		return ItemCombinerMenuSlotDefinition.create()
				.withSlot(0, 35, 35, materialItemTest::test)
				.withSlot(1, 72, 35, additionItemTest::test)
				.withResultSlot(2, 122, 35)
				.build();
	}

	private MarbleRecipeInput createRecipeInput() {
		return new MarbleRecipeInput(this.inputSlots.getItem(0), this.inputSlots.getItem(1));
	}

	public void beginPlacingRecipe() {
		this.placingRecipe = true;
	}

	public void finishPlacingRecipe() {
		this.placingRecipe = false;
		slotsChanged(inputSlots);
	}

	private void shrinkStackInSlot(int slot) {
		ItemStack stack = this.inputSlots.getItem(slot);
		if (!stack.isEmpty()) {
			stack.shrink(1);
			this.inputSlots.setItem(slot, stack);
		}
	}

	public void createResult() {
		MarbleRecipeInput input = this.createRecipeInput();
		Optional<RecipeHolder<MarbleRecipe>> recipe;
		if (this.level instanceof ServerLevel serverLevel) {
			recipe = serverLevel.recipeAccess().getRecipeFor(LosingMyMarblesRecipeTypes.MARBLE, input, serverLevel);
		} else {
			recipe = Optional.empty();
		}

		recipe.ifPresentOrElse(holder -> {
			ItemStack result = holder.value().assemble(input, this.level.registryAccess());
			this.resultSlots.setRecipeUsed(holder);
			this.resultSlots.setItem(0, result);
		}, () -> {
			this.resultSlots.setRecipeUsed(null);
			this.resultSlots.setItem(0, ItemStack.EMPTY);
		});
	}

	private void onTake(Player player, ItemStack stack) {
		stack.onCraftedBy(player, stack.getCount());
		this.resultSlots.awardUsedRecipes(player, List.of(this.inputSlots.getItem(0), this.inputSlots.getItem(1)));
		this.shrinkStackInSlot(0);
		this.shrinkStackInSlot(1);
		this.access.execute((level, pos) -> {
			this.level.playSound(null, pos, SoundEvents.VAULT_REJECT_REWARDED_PLAYER, SoundSource.BLOCKS);
			this.level.setBlockAndUpdate(pos, this.level.getBlockState(pos).setValue(MarbleMakerBlock.ACTIVE, true));
			this.level.scheduleTick(pos, LosingMyMarblesBlocks.MARBLE_MAKER, MarbleMakerBlock.ACTIVATION_TIME);
		});
	}

	@Override
	public void slotsChanged(Container container) {
		if (!placingRecipe) {
			super.slotsChanged(container);
			if (container == this.inputSlots) {
				this.createResult();
			}
			if (this.level instanceof ServerLevel) {
				boolean error = this.getSlot(0).hasItem() && this.getSlot(1).hasItem() && !this.getSlot(this.getResultSlot()).hasItem();
				this.hasRecipeError.set(error ? 1 : 0);
			}
		}
	}

	private boolean isValidBlock(BlockState state) {
		return state.is(LosingMyMarblesBlocks.MARBLE_MAKER);
	}

	public boolean hasRecipeError() {
		return this.hasRecipeError.get() > 0;
	}

	// Recipe Book stuff
	@Override
	public PostPlaceAction handlePlacement(boolean craftAll, boolean creative, RecipeHolder<?> recipeHolder, ServerLevel serverLevel, Inventory inventory) {
		final List<Slot> slots = List.of(getSlot(0), getSlot(1));
		beginPlacingRecipe();

		try {
			return ServerPlaceRecipe.placeRecipe(new ServerPlaceRecipe.CraftingMenuAccess<>() {
				@Override
				public void fillCraftSlotsStackedContents(StackedItemContents stackedItemContents) {
					MarbleMakerMenu.this.fillCraftSlotsStackedContents(stackedItemContents);
				}

				@Override
				public void clearCraftingContent() {
					inputSlots.clearContent();
					resultSlots.clearContent();
				}

				@Override
				public boolean recipeMatches(RecipeHolder<MarbleRecipe> recipeHolder) {
					ItemStack material = MarbleMakerMenu.this.getSlot(0).getItem();
					ItemStack addition = MarbleMakerMenu.this.getSlot(1).getItem();

					return recipeHolder.value().matches(new MarbleRecipeInput(material, addition), serverLevel);
				}
			}, 1, 2, slots, slots, inventory, (RecipeHolder<MarbleRecipe>) recipeHolder, craftAll, creative);
		} finally {
			finishPlacingRecipe();
		}
	}

	@Override
	public void fillCraftSlotsStackedContents(StackedItemContents stackedItemContents) {
		for (ItemStack stack : this.inputSlots) {
			stackedItemContents.accountSimpleStack(stack);
		}
	}

	@NotNull
	@Override
	public RecipeBookType getRecipeBookType() {
		return MarbleMakerRecipeBookHelper.MARBLE_MAKER;
	}

	// ItemCombinerMenu - Copied from there for Recipe Book support
	private void createInputSlots(ItemCombinerMenuSlotDefinition definition) {
		for (final ItemCombinerMenuSlotDefinition.SlotDefinition slotDefinition : definition.getSlots()) {
			this.addSlot(new Slot(this.inputSlots, slotDefinition.slotIndex(), slotDefinition.x(), slotDefinition.y()) {
				@Override
				public boolean mayPlace(ItemStack stack) {
					return slotDefinition.mayPlace().test(stack);
				}
			});
		}
	}

	private void createResultSlot(ItemCombinerMenuSlotDefinition definition) {
		this.addSlot(new Slot(this.resultSlots, definition.getResultSlot().slotIndex(), definition.getResultSlot().x(), definition.getResultSlot().y()) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return false;
			}

			@Override
			public void onTake(Player player, ItemStack itemStack) {
				MarbleMakerMenu.this.onTake(player, itemStack);
			}
		});
	}

	private SimpleContainer createContainer(int size) {
		return new SimpleContainer(size) {
			@Override
			public void setChanged() {
				super.setChanged();
				MarbleMakerMenu.this.slotsChanged(this);
			}
		};
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		this.access.execute((level, pos) -> this.clearContainer(player, this.inputSlots));
	}

	@Override
	public boolean stillValid(Player player) {
		return this.access.evaluate((level, pos) -> this.isValidBlock(level.getBlockState(pos)) && player.canInteractWithBlock(pos, 4.0), true);
	}

	@NotNull
	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		ItemStack stack = ItemStack.EMPTY;
		Slot slot = getSlot(index);
		if (slot != null && slot.hasItem()) {
			ItemStack slotItem = slot.getItem();
			stack = slotItem.copy();
			int slotStart = this.getInventorySlotStart();
			int rowEnd = this.getUseRowEnd();
			if (index == this.getResultSlot()) {
				if (!this.moveItemStackTo(slotItem, slotStart, rowEnd, true)) {
					return ItemStack.EMPTY;
				}

				slot.onQuickCraft(slotItem, stack);
			} else if (index >= 0 && index < this.getResultSlot()) {
				if (!this.moveItemStackTo(slotItem, slotStart, rowEnd, false)) {
					return ItemStack.EMPTY;
				}
			} else if (index >= this.getInventorySlotStart() && index < this.getUseRowEnd()) {
				if (!this.moveItemStackTo(slotItem, 0, this.getResultSlot(), false)) {
					return ItemStack.EMPTY;
				}
			} else if (index >= this.getInventorySlotStart() && index < this.getInventorySlotEnd()) {
				if (!this.moveItemStackTo(slotItem, this.getUseRowStart(), this.getUseRowEnd(), false)) {
					return ItemStack.EMPTY;
				}
			} else if (index >= this.getUseRowStart()
					&& index < this.getUseRowEnd()
					&& !this.moveItemStackTo(slotItem, this.getInventorySlotStart(), this.getInventorySlotEnd(), false)) {
				return ItemStack.EMPTY;
			}

			if (slotItem.isEmpty()) {
				slot.setByPlayer(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}

			if (slotItem.getCount() == stack.getCount()) {
				return ItemStack.EMPTY;
			}

			slot.onTake(player, slotItem);
		}

		return stack;
	}

	public int getResultSlot() {
		return this.resultSlotIndex;
	}

	private int getInventorySlotStart() {
		return this.getResultSlot() + 1;
	}

	private int getInventorySlotEnd() {
		return this.getInventorySlotStart() + 27;
	}

	private int getUseRowStart() {
		return this.getInventorySlotEnd();
	}

	private int getUseRowEnd() {
		return this.getUseRowStart() + 9;
	}
}
