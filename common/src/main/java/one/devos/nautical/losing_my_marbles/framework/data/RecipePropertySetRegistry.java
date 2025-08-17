package one.devos.nautical.losing_my_marbles.framework.data;

import java.util.IdentityHashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipePropertySet;

public final class RecipePropertySetRegistry {
	private static final Map<ResourceKey<RecipePropertySet>, RecipeManager.IngredientExtractor> registered = new IdentityHashMap<>();

	public static ResourceKey<RecipePropertySet> register(ResourceLocation id, RecipeManager.IngredientExtractor extractor) {
		ResourceKey<RecipePropertySet> key = ResourceKey.create(RecipePropertySet.TYPE_KEY, id);
		registered.put(key, extractor);
		return key;
	}

	// called by platform
	public static Map<ResourceKey<RecipePropertySet>, RecipeManager.IngredientExtractor> inject(Map<ResourceKey<RecipePropertySet>, RecipeManager.IngredientExtractor> map) {
		ImmutableMap.Builder<ResourceKey<RecipePropertySet>, RecipeManager.IngredientExtractor> builder = ImmutableMap.builder();
		builder.putAll(map);
		registered.forEach(builder::put);
		return builder.build();
	}
}
