package one.devos.nautical.losing_my_marbles.content.piece.logic.gate;

import java.util.Map;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import one.devos.nautical.losing_my_marbles.content.piece.StraightPieceBlock;

public final class PoweredGatePieceBlock extends StraightPieceBlock {
	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

	public static final VoxelShape CLOSED_DOOR_SHAPE = box(4, 8, 7.9, 12, 14, 8.1);

	public static final Map<Direction.Axis, VoxelShape> CLOSED_HOLES = Shapes.rotateHorizontalAxis(Shapes.join(
			HOLE, CLOSED_DOOR_SHAPE, BooleanOp.ONLY_FIRST
	));

	public PoweredGatePieceBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(POWERED));
	}

	@Override
	protected VoxelShape getHole(BlockState state) {
		return state.getValue(POWERED) ? CLOSED_HOLES.get(state.getValue(AXIS)) : super.getHole(state);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		boolean powered = context.getLevel().hasNeighborSignal(context.getClickedPos());
		return super.getStateForPlacement(context).setValue(POWERED, powered);
	}

	@Override
	protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean moved) {
		if (!(level instanceof ServerLevel serverLevel))
			return;

		boolean powered = state.getValue(POWERED);
		if (powered != level.hasNeighborSignal(pos)) {
			if (powered) {
				level.scheduleTick(pos, this, 4);
			} else {
				toggle(serverLevel, state, pos);
			}
		}
	}

	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (state.getValue(POWERED) && !level.hasNeighborSignal(pos)) {
			toggle(level, state, pos);
		}
	}

	private static void toggle(ServerLevel level, BlockState state, BlockPos pos) {
		level.setBlockAndUpdate(pos, state.cycle(POWERED));
	}
}
