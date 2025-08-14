package one.devos.nautical.losing_my_marbles.content;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import one.devos.nautical.losing_my_marbles.LosingMyMarbles;

public final class LosingMyMarblesBlockTags {
	public static final TagKey<Block> PHYSICS_USES_BASE_SHAPE = create("physics_uses_base_shape");
	public static final TagKey<Block> PIECES_ABOVE_CONNECT = create("pieces_above_connect");
	public static final TagKey<Block> PIECES_BELOW_CONNECT = create("pieces_below_connect");

	private static TagKey<Block> create(String name) {
		return TagKey.create(Registries.BLOCK, LosingMyMarbles.id(name));
	}
}
