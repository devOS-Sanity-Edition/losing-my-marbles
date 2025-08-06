package one.devos.nautical.losing_my_marbles.content.piece;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public abstract class PieceBlock extends Block {
	/**
	 * Typical radius for tube-like pieces. 2 pixels across, 4 pixel diameter.
	 */
	public static final double RADIUS = 2 / 16d;

	public static final EnumProperty<DyeColor> COLOR = EnumProperty.create("color", DyeColor.class);

	protected PieceBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.defaultBlockState().setValue(COLOR, DyeColor.WHITE));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(COLOR);
	}
}
