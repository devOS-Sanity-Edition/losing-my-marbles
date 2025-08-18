package one.devos.nautical.losing_my_marbles.content.piece.logic;

import java.util.Map;
import java.util.function.Function;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import one.devos.nautical.losing_my_marbles.content.marble.MarbleEntity;
import one.devos.nautical.losing_my_marbles.content.piece.PieceBlock;
import one.devos.nautical.losing_my_marbles.framework.block.MarbleListeningBlock;

public final class OneWayGatePieceBlock extends PieceBlock implements MarbleListeningBlock {
	public static final int OPEN_TICKS = 20;

	public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
	public static final BooleanProperty OPEN = BooleanProperty.create("open");

	public static final VoxelShape BASE_HOLE = box(4, 8, 0, 12, 16, 16);
	public static final VoxelShape CLOSED_DOOR_SHAPE = box(4.5, 9, 7.9, 11.5, 16, 8.1);
	public static final VoxelShape OPEN_DOOR_SHAPE = box(4.5, 14.9, 8, 11.5, 15.1, 14.5);

	public static final Map<Direction, VoxelShape> CLOSED_HOLES = Shapes.rotateHorizontal(Shapes.join(
			BASE_HOLE, CLOSED_DOOR_SHAPE, BooleanOp.ONLY_FIRST
	));

	public static final Map<Direction, VoxelShape> OPEN_HOLES = Shapes.rotateHorizontal(Shapes.join(
			BASE_HOLE, OPEN_DOOR_SHAPE, BooleanOp.ONLY_FIRST
	));

	private final Function<BlockState, VoxelShape> shapes;

	public OneWayGatePieceBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.defaultBlockState().setValue(OPEN, false));
		this.shapes = this.getShapeForEachState(shapeFactory(state -> {
			boolean open = state.getValue(OPEN);
			Map<Direction, VoxelShape> map = open ? OPEN_HOLES : CLOSED_HOLES;
			return map.get(state.getValue(FACING));
		}));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(FACING, OPEN));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return super.getStateForPlacement(context).setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return this.shapes.apply(state);
	}

	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (state.getValue(OPEN)) {
			toggle(level, state, pos);
		}
	}

	@Override
	public void marbleEntered(ServerLevel level, BlockState state, BlockPos pos, MarbleEntity entity) {
		Direction facing = state.getValue(FACING);

		if (facing.getUnitVec3().dot(entity.getDeltaMovement()) > 0)
			return; // moving the wrong way

		if (!state.getValue(OPEN)) {
			toggle(level, state, pos);
		}

		// cancel any previous scheduled ticks
		level.getBlockTicks().clearArea(new BoundingBox(pos));
		level.scheduleTick(pos, this, OPEN_TICKS);
	}

	private static void toggle(ServerLevel level, BlockState state, BlockPos pos) {
		level.setBlockAndUpdate(pos, state.cycle(OPEN));
	}
}
