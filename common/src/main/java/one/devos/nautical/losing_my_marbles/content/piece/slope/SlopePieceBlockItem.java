package one.devos.nautical.losing_my_marbles.content.piece.slope;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

public final class SlopePieceBlockItem extends BlockItem {
	public SlopePieceBlockItem(Block block, Properties properties) {
		super(block, properties);
	}

	@Override
	protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		BlockPos upperPos = SlopePieceBlock.getOtherHalfPos(state, pos);
		boolean placedUpper = false;

		if (upperPos.getY() <= level.getMaxY() && level.getBlockState(upperPos).canBeReplaced(context)) {
			BlockPlaceContext upperContext = BlockPlaceContext.at(context, upperPos, Direction.UP);
			BlockState upperState = state.getBlock().getStateForPlacement(upperContext);
			if (upperState != null) {
				upperState = upperState.setValue(SlopePieceBlock.HALF, DoubleBlockHalf.UPPER);
				placedUpper = level.setBlock(upperPos, upperState, Block.UPDATE_ALL_IMMEDIATE);
			}
		}

		return placedUpper && super.placeBlock(context, state);
	}
}
