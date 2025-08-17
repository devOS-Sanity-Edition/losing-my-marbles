package one.devos.nautical.losing_my_marbles.content.marble.recipe;

import java.util.List;
import java.util.Optional;

import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesBlocks;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;
import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesRecipeSerializers;
import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesRecipeTypes;

public final class MarbleRecipe implements Recipe<MarbleRecipeInput> {
	public final Ingredient material;
	public final Optional<Ingredient> addition;

	private final String group;
	private final ItemStack result;

	@Nullable
	private PlacementInfo placementInfo;

	public MarbleRecipe(String group, Ingredient material, Optional<Ingredient> addition, ItemStack result) {
		this.group = group;
		this.material = material;
		this.addition = addition;
		this.result = result;
	}

	@Override
	public boolean matches(MarbleRecipeInput input, Level level) {
		return this.material.test(input.material()) && Ingredient.testOptionalIngredient(this.addition, input.addition());
	}

	@Override
	public ItemStack assemble(MarbleRecipeInput input, HolderLookup.Provider registries) {
		return this.result.copy();
	}

	@Override
	public String group() {
		return this.group;
	}

	@Override
	public RecipeSerializer<? extends Recipe<MarbleRecipeInput>> getSerializer() {
		return LosingMyMarblesRecipeSerializers.MARBLE;
	}

	@Override
	public RecipeType<? extends Recipe<MarbleRecipeInput>> getType() {
		return LosingMyMarblesRecipeTypes.MARBLE;
	}

	@Override
	public PlacementInfo placementInfo() {
		if (this.placementInfo == null) {
			this.placementInfo = PlacementInfo.createFromOptionals(List.of(Optional.of(this.material), this.addition));
		}

		return this.placementInfo;
	}

	@Override
	public List<RecipeDisplay> display() {
		return List.of(new MarbleRecipeDisplay(
				this.material.display(),
				Ingredient.optionalIngredientToDisplay(this.addition),
				new SlotDisplay.ItemStackSlotDisplay(this.result),
				new SlotDisplay.ItemSlotDisplay(LosingMyMarblesBlocks.MARBLE_MAKER.asItem())
		));
	}

	@Override
	public RecipeBookCategory recipeBookCategory() {
		// TODO thunder
		return RecipeBookCategories.SMITHING;
	}

	public enum Serializer implements RecipeSerializer<MarbleRecipe> {
		INSTANCE;

		public static final MapCodec<MarbleRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
				Codec.STRING.optionalFieldOf("group", "").forGetter(MarbleRecipe::group),
				Ingredient.CODEC.fieldOf("material").forGetter(recipe -> recipe.material),
				Ingredient.CODEC.optionalFieldOf("addition").forGetter(recipe -> recipe.addition),
				ItemStack.STRICT_CODEC.fieldOf("result").forGetter(recipe -> recipe.result)
		).apply(i, MarbleRecipe::new));

		public static final StreamCodec<RegistryFriendlyByteBuf, MarbleRecipe> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.STRING_UTF8, MarbleRecipe::group,
				Ingredient.CONTENTS_STREAM_CODEC, recipe -> recipe.material,
				Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC, recipe -> recipe.addition,
				ItemStack.STREAM_CODEC, recipe -> recipe.result,
				MarbleRecipe::new
		);

		@Override
		public MapCodec<MarbleRecipe> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, MarbleRecipe> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
