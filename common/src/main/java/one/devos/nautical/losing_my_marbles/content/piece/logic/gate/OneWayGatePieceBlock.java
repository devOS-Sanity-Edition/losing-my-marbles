package one.devos.nautical.losing_my_marbles.content.piece.logic.gate;

import java.util.Map;
import java.util.function.Function;

import com.github.stephengold.joltjni.Quat;
import com.github.stephengold.joltjni.ShapeRefC;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import one.devos.nautical.losing_my_marbles.content.marble.MarbleEntity;
import one.devos.nautical.losing_my_marbles.content.piece.PieceBlock;
import one.devos.nautical.losing_my_marbles.content.piece.StraightPieceBlock;
import one.devos.nautical.losing_my_marbles.framework.block.MarbleListeningBlock;
import one.devos.nautical.losing_my_marbles.framework.phys.terrain.collision.PhysicsCollision;
import one.devos.nautical.losing_my_marbles.framework.phys.util.TriStripBuilder;

public final class OneWayGatePieceBlock extends PieceBlock implements MarbleListeningBlock {
	public static final int OPEN_TICKS = 20;

	public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
	public static final BooleanProperty OPEN = BooleanProperty.create("open");

	public static final Map<Direction, VoxelShape> HOLES = Shapes.rotateHorizontal(StraightPieceBlock.HOLE);

	private final Function<BlockState, VoxelShape> shapes;

	public OneWayGatePieceBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.defaultBlockState().setValue(OPEN, false));
		this.shapes = this.getShapeForEachState(shapeFactory(state -> HOLES.get(state.getValue(FACING))));
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
	protected boolean skipRendering(BlockState state, BlockState adjacentBlockState, Direction side) {
		return super.skipRendering(state, adjacentBlockState, side) && side.getAxis() == state.getValue(FACING).getAxis();
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

	public static void additionalCollision(BlockState state, PhysicsCollision.Provider.Output output) {
		TriStripBuilder builder = new TriStripBuilder(PieceBlock::pixelsToBlocks);

		builder.then(-4, 0, 0).then(-4, 8, 0)
				.then(4, 0, 0).then(4, 8, 0);

		float yRot = Mth.DEG_TO_RAD * switch (state.getValue(FACING)) {
			case SOUTH -> 0;
			case EAST -> 90;
			case NORTH -> 180;
			case WEST -> 270;
			default -> throw new IllegalStateException("Illegal direction");
		};

		Quat rotation = Quat.sEulerAngles(0, yRot, 0);

		try (ShapeRefC shape = builder.build()) {
			output.accept(rotation, shape);
		}
	}

	private static void toggle(ServerLevel level, BlockState state, BlockPos pos) {
		level.setBlockAndUpdate(pos, state.cycle(OPEN));
	}
}
