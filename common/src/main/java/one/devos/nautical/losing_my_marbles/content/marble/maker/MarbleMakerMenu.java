package one.devos.nautical.losing_my_marbles.content.marble.maker;

import java.util.List;
import java.util.Optional;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition;
import net.minecraft.world.item.ItemStack;
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

public final class MarbleMakerMenu extends ItemCombinerMenu {
	private final Level level;
	private final DataSlot hasRecipeError = DataSlot.standalone();

	public MarbleMakerMenu(int containerId, Inventory inventory, ContainerLevelAccess access, Level level) {
		super(LosingMyMarblesMenus.MARBLE_MAKER, containerId, inventory, access, createInputSlotDefinitions(level.recipeAccess()));
		this.level = level;
		this.addDataSlot(this.hasRecipeError).set(0);
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

	private void shrinkStackInSlot(int slot) {
		ItemStack stack = this.inputSlots.getItem(slot);
		if (!stack.isEmpty()) {
			stack.shrink(1);
			this.inputSlots.setItem(slot, stack);
		}
	}

	@Override
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

	@Override
	protected void onTake(Player player, ItemStack stack) {
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
		super.slotsChanged(container);
		if (this.level instanceof ServerLevel) {
			boolean error = this.getSlot(0).hasItem() && this.getSlot(1).hasItem() && !this.getSlot(this.getResultSlot()).hasItem();
			this.hasRecipeError.set(error ? 1 : 0);
		}
	}

	@Override
	protected boolean isValidBlock(BlockState state) {
		return state.is(LosingMyMarblesBlocks.MARBLE_MAKER);
	}

	public boolean hasRecipeError() {
		return this.hasRecipeError.get() > 0;
	}
}
