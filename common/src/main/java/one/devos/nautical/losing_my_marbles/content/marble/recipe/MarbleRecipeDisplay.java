package one.devos.nautical.losing_my_marbles.content.marble.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;

public record MarbleRecipeDisplay(SlotDisplay material, SlotDisplay addition, SlotDisplay result, SlotDisplay craftingStation) implements RecipeDisplay {
	public static final MapCodec<MarbleRecipeDisplay> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			SlotDisplay.CODEC.fieldOf("material").forGetter(MarbleRecipeDisplay::material),
			SlotDisplay.CODEC.fieldOf("addition").forGetter(MarbleRecipeDisplay::addition),
			SlotDisplay.CODEC.fieldOf("result").forGetter(MarbleRecipeDisplay::result),
			SlotDisplay.CODEC.fieldOf("crafting_station").forGetter(MarbleRecipeDisplay::craftingStation)
	).apply(i, MarbleRecipeDisplay::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, MarbleRecipeDisplay> STREAM_CODEC = StreamCodec.composite(
			SlotDisplay.STREAM_CODEC, MarbleRecipeDisplay::material,
			SlotDisplay.STREAM_CODEC, MarbleRecipeDisplay::addition,
			SlotDisplay.STREAM_CODEC, MarbleRecipeDisplay::result,
			SlotDisplay.STREAM_CODEC, MarbleRecipeDisplay::craftingStation,
			MarbleRecipeDisplay::new
	);

	public static final Type<MarbleRecipeDisplay> TYPE = new Type<>(CODEC, STREAM_CODEC);

	@Override
	public Type<? extends RecipeDisplay> type() {
		return TYPE;
	}
}
