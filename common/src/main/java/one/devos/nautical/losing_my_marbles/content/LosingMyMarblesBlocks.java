package one.devos.nautical.losing_my_marbles.content;

import java.util.function.Function;

import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import one.devos.nautical.losing_my_marbles.LosingMyMarbles;
import one.devos.nautical.losing_my_marbles.content.marble.maker.MarbleMakerBlock;
import one.devos.nautical.losing_my_marbles.content.piece.CornerPieceBlock;
import one.devos.nautical.losing_my_marbles.content.piece.IntersectionPieceBlock;
import one.devos.nautical.losing_my_marbles.content.piece.StraightPieceBlock;
import one.devos.nautical.losing_my_marbles.content.piece.TubePieceBlock;
import one.devos.nautical.losing_my_marbles.content.piece.logic.SplitterPieceBlock;
import one.devos.nautical.losing_my_marbles.framework.phys.terrain.collision.CustomPhysicsCollisionRegistry;
import one.devos.nautical.losing_my_marbles.framework.phys.terrain.collision.DefaultCollisionSource;
import one.devos.nautical.losing_my_marbles.framework.platform.Env;
import one.devos.nautical.losing_my_marbles.framework.platform.PlatformClientHelper;
import one.devos.nautical.losing_my_marbles.framework.platform.PlatformHelper;

public class LosingMyMarblesBlocks {
	public static final MarbleMakerBlock MARBLE_MAKER = register("marble_maker", MarbleMakerBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK));

	public static final StraightPieceBlock STRAIGHT_PIECE = register("straight_piece", StraightPieceBlock::new, pieceProperties());
	public static final IntersectionPieceBlock INTERSECTION_PIECE = register("intersection_piece", IntersectionPieceBlock::new, pieceProperties().dynamicShape());
	public static final CornerPieceBlock CORNER_PIECE = register("corner_piece", CornerPieceBlock::new, pieceProperties().dynamicShape());
	public static final TubePieceBlock TUBE_PIECE = register("tube_piece", TubePieceBlock::new, pieceProperties());
	public static final SplitterPieceBlock SPLITTER_PIECE = register(
			"splitter_piece", properties -> new SplitterPieceBlock(properties, CORNER_PIECE), pieceProperties().dynamicShape()
	);

	private static BlockBehaviour.Properties pieceProperties() {
		return BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS)
				.sound(SoundType.RESIN_BRICKS)
				.instrument(NoteBlockInstrument.BASEDRUM);
	}

	static <T extends Block> T register(String name, Function<BlockBehaviour.Properties, T> factory, BlockBehaviour.Properties properties) {
		ResourceLocation id = LosingMyMarbles.id(name);
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, id);
		T block = Registry.register(BuiltInRegistries.BLOCK, id, factory.apply(properties.setId(key)));

		LosingMyMarblesItems.register(name, itemProperties -> new BlockItem(block, itemProperties), new Item.Properties().useBlockDescriptionPrefix());

		return block;
	}

	public static void init() {
		CustomPhysicsCollisionRegistry.register(CORNER_PIECE, DefaultCollisionSource.COLLISION_SHAPE, CornerPieceBlock::additionalCollision);
		CustomPhysicsCollisionRegistry.register(INTERSECTION_PIECE, DefaultCollisionSource.COLLISION_SHAPE, IntersectionPieceBlock::additionalCollision);
		CustomPhysicsCollisionRegistry.register(SPLITTER_PIECE, DefaultCollisionSource.COLLISION_SHAPE, SPLITTER_PIECE::additionalCollision);

		if (PlatformHelper.INSTANCE.getEnvironment() == Env.CLIENT) {
			PlatformClientHelper.INSTANCE.setBlockRenderLayer(ChunkSectionLayer.TRANSLUCENT,
					STRAIGHT_PIECE,
					INTERSECTION_PIECE,
					CORNER_PIECE,
					TUBE_PIECE,
					SPLITTER_PIECE
			);
		}
	}
}
