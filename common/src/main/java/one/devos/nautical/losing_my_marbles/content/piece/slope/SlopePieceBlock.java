package one.devos.nautical.losing_my_marbles.content.piece.slope;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import one.devos.nautical.losing_my_marbles.content.piece.PieceBlock;

public final class SlopePieceBlock extends PieceBlock {
	public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
	public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

	public SlopePieceBlock(Properties properties) {
		super(properties);
	}

	public static BlockPos getOtherHalfPos(BlockState state, BlockPos pos) {
		Direction facing = state.getValue(FACING);
		return state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos.above().relative(facing.getOpposite()) : pos.below().relative(facing);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(FACING, HALF);
	}

	@Override
	public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
		BlockPos otherHalfPos = getOtherHalfPos(state, pos);
		BlockState otherHalfState = level.getBlockState(otherHalfPos);
		if (otherHalfState.is(this)) {
			level.removeBlock(otherHalfPos, false);
			level.levelEvent(null, LevelEvent.PARTICLES_DESTROY_BLOCK, otherHalfPos, Block.getId(otherHalfState));
		}
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return super.getStateForPlacement(context)
				.setValue(FACING, context.getHorizontalDirection().getOpposite())
				.setValue(HALF, DoubleBlockHalf.LOWER);
	}
}
