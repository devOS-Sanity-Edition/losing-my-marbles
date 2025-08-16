package one.devos.nautical.losing_my_marbles.content.marble.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public record MarbleRecipeInput(ItemStack material, ItemStack addition) implements RecipeInput {
	@Override
	public ItemStack getItem(int i) {
		return switch (i) {
			case 0 -> this.material;
			case 1 -> this.addition;
			default -> throw new IndexOutOfBoundsException(i);
		};
	}

	@Override
	public int size() {
		return 2;
	}
}
