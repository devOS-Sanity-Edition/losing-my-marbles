package one.devos.nautical.losing_my_marbles.content.piece.logic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import one.devos.nautical.losing_my_marbles.content.marble.MarbleEntity;
import one.devos.nautical.losing_my_marbles.content.piece.StraightPieceBlock;
import one.devos.nautical.losing_my_marbles.framework.block.MarbleListeningBlock;

public final class DetectorPieceBlock extends StraightPieceBlock implements MarbleListeningBlock {
	public static final int ACTIVE_TICKS = 10;
	public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

	public DetectorPieceBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.defaultBlockState().setValue(ACTIVE, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(ACTIVE));
	}

	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (state.getValue(ACTIVE)) {
			toggle(level, state, pos);
		}
	}

	@Override
	protected boolean isSignalSource(BlockState state) {
		return true;
	}

	@Override
	protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
		return state.getValue(ACTIVE) ? 15 : 0;
	}

	@Override
	public void marbleEntered(ServerLevel level, BlockState state, BlockPos pos, MarbleEntity entity) {
		if (!state.getValue(ACTIVE)) {
			toggle(level, state, pos);
		}

		// cancel any previous scheduled ticks
		level.getBlockTicks().clearArea(new BoundingBox(pos));
		level.scheduleTick(pos, this, ACTIVE_TICKS);
	}

	private static void toggle(ServerLevel level, BlockState state, BlockPos pos) {
		level.setBlockAndUpdate(pos, state.cycle(ACTIVE));
	}
}
