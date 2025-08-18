package one.devos.nautical.losing_my_marbles.content.marble.recipe.book;

import com.mojang.serialization.MapCodec;

import net.minecraft.stats.RecipeBookSettings.TypeSettings;
import one.devos.nautical.losing_my_marbles.framework.platform.PlatformHelper;

public class MarbleRecipeBookSettings {
	public static final MapCodec<TypeSettings> MARBLE_MAKER_MAP_CODEC = PlatformHelper.INSTANCE.createTypeSettingsCodec("isMarbleMakerGuiOpen", "isMarbleMakerFilteringCraftable");
}
