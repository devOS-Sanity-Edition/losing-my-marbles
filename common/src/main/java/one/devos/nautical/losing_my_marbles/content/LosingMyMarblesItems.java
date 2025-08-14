package one.devos.nautical.losing_my_marbles.content;

import java.util.function.Function;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import one.devos.nautical.losing_my_marbles.LosingMyMarbles;
import one.devos.nautical.losing_my_marbles.content.marble.MarbleItem;
import one.devos.nautical.losing_my_marbles.content.marble.StoredMarble;
import one.devos.nautical.losing_my_marbles.content.marble.data.MarbleType;

public class LosingMyMarblesItems {
	public static final Item MARBLE = register(
			"marble", MarbleItem::new, new Item.Properties()
					.stacksTo(1)
					.component(LosingMyMarblesDataComponents.MARBLE, new StoredMarble.Type(MarbleType.DEFAULT))
	);

	static <T extends Item> T register(String name, Function<Item.Properties, T> factory, Item.Properties properties) {
		ResourceLocation id = LosingMyMarbles.id(name);
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
		return Registry.register(BuiltInRegistries.ITEM, id, factory.apply(properties.setId(key)));
	}

	public static void init() {
	}
}
