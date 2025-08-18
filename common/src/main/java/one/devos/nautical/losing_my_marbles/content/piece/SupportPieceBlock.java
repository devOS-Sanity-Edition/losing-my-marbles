package one.devos.nautical.losing_my_marbles.content.piece;

import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import one.devos.nautical.losing_my_marbles.framework.block.DyeableTransparentBlock;

public final class SupportPieceBlock extends DyeableTransparentBlock {
	public static final EnumProperty<Part> PART = EnumProperty.create("part", Part.class);

	public static final VoxelShape COLUMN = box(4, 0, 4, 12, 16, 12);
	public static final VoxelShape BASE = box(0, 0, 0, 16, 5, 16);
	public static final VoxelShape TOP = box(0, 14, 0, 16, 16, 16);

	public static final Map<Part, VoxelShape> SHAPES = Util.makeEnumMap(Part.class, part -> switch (part) {
		case SINGLE -> Shapes.join(
				Shapes.or(BASE, COLUMN, SupportPieceBlock.TOP),
				PieceBlock.BOTTOM_WITH_LEGS_CUT,
				BooleanOp.ONLY_FIRST
		);
		case TOP -> Shapes.or(COLUMN, TOP);
		case MIDDLE -> COLUMN;
		case BOTTOM -> Shapes.join(
				Shapes.or(BASE, COLUMN),
				PieceBlock.BOTTOM_WITH_LEGS_CUT,
				BooleanOp.ONLY_FIRST
		);
	});

	private final Function<BlockState, VoxelShape> shapes;

	public SupportPieceBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.defaultBlockState().setValue(PART, Part.SINGLE));
		this.shapes = this.getShapeForEachState(state -> SHAPES.get(state.getValue(PART)));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(PART));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();

		boolean supportAbove = level.getBlockState(pos.above()).is(this);
		boolean supportBelow = level.getBlockState(pos.below()).is(this);
		Part part = Part.get(supportAbove, supportBelow);

		return this.defaultBlockState().setValue(PART, part);
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
									 Direction side, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
		Part part = state.getValue(PART);
		boolean supportAbove = part.supportAbove;
		boolean supportBelow = part.supportBelow;

		if (side == Direction.UP) {
			supportAbove = neighborState.is(this);
		} else if (side == Direction.DOWN) {
			supportBelow = neighborState.is(this);
		}

		return state.setValue(PART, Part.get(supportAbove, supportBelow));
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return this.shapes.apply(state);
	}

	public enum Part implements StringRepresentable {
		SINGLE(false, false),
		BOTTOM(true, false),
		MIDDLE(true, true),
		TOP(false, true);

		public final boolean supportAbove;
		public final boolean supportBelow;

		private final String name = this.name().toLowerCase(Locale.ROOT);

		Part(boolean supportAbove, boolean supportBelow) {
			this.supportAbove = supportAbove;
			this.supportBelow = supportBelow;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}

		public static Part get(boolean supportAbove, boolean supportBelow) {
			if (supportAbove && supportBelow) {
				return MIDDLE;
			} else if (supportAbove) {
				return BOTTOM;
			} else if (supportBelow) {
				return TOP;
			} else {
				return SINGLE;
			}
		}
	}
}
