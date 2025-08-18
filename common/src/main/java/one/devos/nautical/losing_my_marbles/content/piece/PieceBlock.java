package one.devos.nautical.losing_my_marbles.content.piece;

import java.util.function.Function;

import org.jetbrains.annotations.NotNull;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesBlockTags;
import one.devos.nautical.losing_my_marbles.framework.block.DyeableTransparentBlock;

public abstract class PieceBlock extends DyeableTransparentBlock {
	public static final BooleanProperty TOP_CONNECTIONS = BooleanProperty.create("top_connections");
	public static final BooleanProperty BOTTOM_CONNECTIONS = BooleanProperty.create("bottom_connections");

	public static final VoxelShape BOTTOM_CUT = box(0, 0, 0, 16, 3, 16);
	public static final VoxelShape BOTTOM_WITH_LEGS_CUT = Shapes.or(Block.column(16, 8, 0, 3), Block.column(8, 16, 0, 3));

	public static final VoxelShape LEGLESS_INTERACTION_SHAPE = Shapes.join(Shapes.block(), BOTTOM_CUT, BooleanOp.ONLY_FIRST);

	protected PieceBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.defaultBlockState().setValue(COLOR, DyeColor.WHITE));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(COLOR, TOP_CONNECTIONS, BOTTOM_CONNECTIONS);
	}

	@Override
	protected boolean skipRendering(BlockState state, BlockState adjacentBlockState, Direction side) {
		return adjacentBlockState.getBlock() instanceof PieceBlock;
	}

	@Override
	protected VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
		return state.getValue(BOTTOM_CONNECTIONS) ? Shapes.block() : LEGLESS_INTERACTION_SHAPE;
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		// super method loses the context
		return this.getShape(state, level, pos, context);
	}

	@Override
	@NotNull
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState state = this.defaultBlockState();
		Level level = context.getLevel();

		BlockPos below = context.getClickedPos().below();
		state = state.setValue(BOTTOM_CONNECTIONS, connectBelow(level, level.getBlockState(below), below));

		BlockPos above = context.getClickedPos().above();
		state = state.setValue(TOP_CONNECTIONS, connectAbove(level.getBlockState(above)));

		return state;
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
									 Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
		if (direction == Direction.DOWN) {
			return state.setValue(BOTTOM_CONNECTIONS, connectBelow(level, neighborState, neighborPos));
		} else if (direction == Direction.UP) {
			return state.setValue(TOP_CONNECTIONS, connectAbove(neighborState));
		} else {
			return state;
		}
	}

	public static boolean connectBelow(BlockGetter level, BlockState state, BlockPos pos) {
		return state.is(LosingMyMarblesBlockTags.PIECES_ABOVE_CONNECT) || state.isFaceSturdy(level, pos, Direction.UP, SupportType.FULL);
	}

	public static boolean connectAbove(BlockState state) {
		return state.is(LosingMyMarblesBlockTags.PIECES_BELOW_CONNECT);
	}

	public static float pixelsToBlocks(float pixels) {
		return pixels / 16;
	}

	protected static Function<BlockState, VoxelShape> shapeFactory(Function<BlockState, VoxelShape> holeProvider) {
		return state -> {
			VoxelShape hole = holeProvider.apply(state);
			VoxelShape bottomCut = state.getValue(BOTTOM_CONNECTIONS) ? BOTTOM_WITH_LEGS_CUT : BOTTOM_CUT;
			VoxelShape wholeCut = Shapes.or(hole, bottomCut);
			return Shapes.join(Shapes.block(), wholeCut, BooleanOp.ONLY_FIRST);
		};
	}
}
