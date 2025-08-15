package one.devos.nautical.losing_my_marbles.content;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import one.devos.nautical.losing_my_marbles.LosingMyMarbles;

public final class LosingMyMarblesItemTags {
	public static final TagKey<Item> EXTRA_MARBLE_KNOCKBACK = create("extra_marble_knockback");

	private static TagKey<Item> create(String name) {
		return TagKey.create(Registries.ITEM, LosingMyMarbles.id(name));
	}
}
