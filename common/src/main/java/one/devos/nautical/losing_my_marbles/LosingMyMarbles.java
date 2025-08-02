package one.devos.nautical.losing_my_marbles;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public final class LosingMyMarbles {
	public static final String ID = "losing_my_marbles";

	public static void init() {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id("test_item"));
		Item.Properties properties = new Item.Properties().setId(key);
		Item item = new Item(properties);
		Registry.register(BuiltInRegistries.ITEM, key.location(), item);
	}

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(ID, path);
	}
}
