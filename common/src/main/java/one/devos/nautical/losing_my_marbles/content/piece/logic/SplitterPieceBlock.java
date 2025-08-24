package one.devos.nautical.losing_my_marbles.content.piece.logic;

import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import one.devos.nautical.losing_my_marbles.content.marble.MarbleEntity;
import one.devos.nautical.losing_my_marbles.content.piece.CornerPieceBlock;
import one.devos.nautical.losing_my_marbles.content.piece.PieceBlock;
import one.devos.nautical.losing_my_marbles.framework.block.MarbleListeningBlock;
import one.devos.nautical.losing_my_marbles.framework.phys.terrain.collision.PhysicsCollision;
import one.devos.nautical.losing_my_marbles.framework.phys.terrain.collision.PhysicsCollisionContext;

public final class SplitterPieceBlock extends PieceBlock implements MarbleListeningBlock {
	public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
	public static final EnumProperty<Side> SIDE = EnumProperty.create("side", Side.class);

	public static final Map<Direction, VoxelShape> NORMAL_HOLES = Shapes.rotateHorizontal(Shapes.or(
			box(0, 8, 4, 16, 16, 12),
			box(4, 8, 0, 12, 16, 12)
	));

	private final Function<BlockState, VoxelShape> shapes;
	private final UnaryOperator<BlockState> asCorner;

	public SplitterPieceBlock(Properties properties, CornerPieceBlock cornerBlock) {
		super(properties);
		this.shapes = this.getShapeForEachState(shapeFactory(state -> NORMAL_HOLES.get(state.getValue(FACING))));

		Map<BlockState, BlockState> toCornerMap = new IdentityHashMap<>();
		for (BlockState state : this.stateDefinition.getPossibleStates()) {
			toCornerMap.put(state, asCorner(state, cornerBlock));
		}
		this.asCorner = toCornerMap::get;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(FACING, SIDE));
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		if (context instanceof PhysicsCollisionContext) {
			return this.asCorner.apply(state).getShape(level, pos, context);
		} else {
			return this.shapes.apply(state);
		}
	}

	@Override
	protected boolean skipRendering(BlockState state, BlockState adjacentBlockState, Direction side) {
		if (super.skipRendering(state, adjacentBlockState, side)) {
			Direction facing = state.getValue(FACING);
			return side == facing || side == facing.getClockWise() || side == facing.getCounterClockWise();
		} else {
			return false;
		}
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return super.getStateForPlacement(context).setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override
	public void marbleEntered(ServerLevel level, BlockState state, BlockPos pos, MarbleEntity entity) {
		level.setBlockAndUpdate(pos, state.cycle(SIDE));
	}

	public void additionalCollision(BlockState state, PhysicsCollision.Provider.Output output) {
		CornerPieceBlock.additionalCollision(this.asCorner.apply(state), output);
	}

	private static BlockState asCorner(BlockState state, Block cornerBlock) {
		Direction facing = state.getValue(FACING);
		Side side = state.getValue(SIDE);

		CornerPieceBlock.Facing cornerFacing = CornerPieceBlock.Facing.fromDirections(facing.getOpposite(), side.getDirection(facing));

		return cornerBlock.defaultBlockState().setValue(CornerPieceBlock.FACING, cornerFacing);
	}

	public enum Side implements StringRepresentable {
		LEFT {
			@Override
			public Direction getDirection(Direction facing) {
				return facing.getClockWise();
			}
		},
		RIGHT {
			@Override
			public Direction getDirection(Direction facing) {
				return facing.getCounterClockWise();
			}
		};

		private final String name = this.name().toLowerCase(Locale.ROOT);

		@Override
		public String getSerializedName() {
			return this.name;
		}

		public abstract Direction getDirection(Direction facing);
	}
}
