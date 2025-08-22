package one.devos.nautical.losing_my_marbles.content.piece.logic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import one.devos.nautical.losing_my_marbles.content.marble.MarbleEntity;
import one.devos.nautical.losing_my_marbles.content.piece.StraightPieceBlock;

public final class DetectorPieceBlock extends StraightPieceBlock {
	public static final int ACTIVE_TICKS = 10;
	public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

	public static final AABB DETECTION_AREA = box(4, 8, 6, 12, 14, 10).toAabbs().getFirst();

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
	protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier applier) {
		if (!(level instanceof ServerLevel serverLevel) || !(entity instanceof MarbleEntity))
			return;

		AABB detectionArea = DETECTION_AREA.move(pos);
		if (!entity.getBoundingBox().intersects(detectionArea))
			return;

		if (!state.getValue(ACTIVE)) {
			toggle(serverLevel, state, pos);
		}

		// cancel any previous scheduled ticks
		serverLevel.getBlockTicks().clearArea(new BoundingBox(pos));
		serverLevel.scheduleTick(pos, this, ACTIVE_TICKS);
	}

	private static void toggle(ServerLevel level, BlockState state, BlockPos pos) {
		level.setBlockAndUpdate(pos, state.cycle(ACTIVE));
	}
}
