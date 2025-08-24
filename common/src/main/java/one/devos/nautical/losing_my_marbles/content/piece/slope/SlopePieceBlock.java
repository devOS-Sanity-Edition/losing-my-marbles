package one.devos.nautical.losing_my_marbles.content.piece.slope;

import java.util.Map;
import java.util.function.Function;

import com.github.stephengold.joltjni.Quat;
import com.github.stephengold.joltjni.ShapeRefC;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import one.devos.nautical.losing_my_marbles.content.piece.PieceBlock;
import one.devos.nautical.losing_my_marbles.framework.phys.terrain.collision.PhysicsCollision;
import one.devos.nautical.losing_my_marbles.framework.phys.util.CurveGenerator;
import one.devos.nautical.losing_my_marbles.framework.phys.util.TriStripBuilder;

public final class SlopePieceBlock extends PieceBlock {
	public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
	public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

	public static final Map<DoubleBlockHalf, Map<Direction, VoxelShape>> HOLES = Util.makeEnumMap(DoubleBlockHalf.class, half -> switch (half) {
		case UPPER -> Shapes.rotateHorizontal(Shapes.or(
				box(4, 8, 0, 12, 16, 16),
				box(4, 4, 0, 12, 8, 15),
				box(4, 0, 0, 12, 4, 10)
		));
		case LOWER -> Shapes.rotateHorizontal(box(4, 8, 0, 12, 16, 16));
	});

	private final Function<BlockState, VoxelShape> shapes;

	public SlopePieceBlock(Properties properties) {
		super(properties);
		this.shapes = this.getShapeForEachState(shapeFactory(state -> {
			Map<Direction, VoxelShape> byFacing = HOLES.get(state.getValue(HALF));
			return byFacing.get(state.getValue(FACING));
		}));
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

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return this.shapes.apply(state);
	}

	@Override
	protected boolean skipRendering(BlockState state, BlockState adjacentBlockState, Direction side) {
		return super.skipRendering(state, adjacentBlockState, side) && side.getAxis() == state.getValue(FACING).getAxis();
	}

	public static void additionalCollision(BlockState state, PhysicsCollision.Provider.Output output) {
		TriStripBuilder builder = new TriStripBuilder(PieceBlock::pixelsToBlocks);
		DoubleBlockHalf half = state.getValue(HALF);

		if (half == DoubleBlockHalf.LOWER)
			builder.flip();

		CurveGenerator curve = switch (half) {
			case UPPER -> new CurveGenerator(5, -8, 8, Mth.HALF_PI, Mth.PI / 4, 4);
			case LOWER -> new CurveGenerator(-5, 8, 8, Mth.HALF_PI * 3, Mth.PI * 10 / 8, 4);
		};

		curve.forEachPoint((x, z) -> builder.then(-4, z, x).then(4, z, x));

		switch (half) {
			case LOWER -> builder.then(-4, 8, 8).then(4, 8, 8);
			case UPPER -> builder.then(-4, -8, -8).then(4, -8, -8);
		}

		float yRot = Mth.DEG_TO_RAD * switch (state.getValue(FACING)) {
			case SOUTH -> 180;
			case EAST -> 270;
			case NORTH -> 0;
			case WEST -> 90;
			default -> throw new IllegalStateException("Illegal direction");
		};

		Quat rotation = Quat.sEulerAngles(0, yRot, 0);

		try (ShapeRefC shape = builder.build()) {
			output.accept(rotation, shape);
		}
	}
}
