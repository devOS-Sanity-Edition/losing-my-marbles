package one.devos.nautical.losing_my_marbles.content.piece;

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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import one.devos.nautical.losing_my_marbles.framework.phys.terrain.collision.PhysicsCollision;
import one.devos.nautical.losing_my_marbles.framework.phys.util.CurveGenerator;
import one.devos.nautical.losing_my_marbles.framework.phys.util.TriStripBuilder;

public final class HalfPipePieceBlock extends PieceBlock {
	public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
	public static final EnumProperty<Half> OPENING = EnumProperty.create("opening", Half.class);

	public static final Map<Half, Map<Direction, VoxelShape>> HOLES = Util.makeEnumMap(Half.class, half -> switch (half) {
		case TOP -> Shapes.rotateHorizontal(box(4, 8, 0, 12, 16, 12));
		case BOTTOM -> Shapes.rotateHorizontal(Shapes.or(
				box(4, 7.75, 0, 12, 16, 12), // top hole, going inwards
				box(4, 0, 4, 12, 16, 12), // vertical hole, through the bottom
				box(4, 4, 1, 12, 8, 4) // extra cut to make sure the slope is fully exposed
		));
	});

	private final Function<BlockState, VoxelShape> shapes;

	public HalfPipePieceBlock(Properties properties) {
		super(properties);
		this.shapes = this.getShapeForEachState(shapeFactory(state -> {
			Map<Direction, VoxelShape> byFacing = HOLES.get(state.getValue(OPENING));
			return byFacing.get(state.getValue(FACING));
		}));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(FACING, OPENING));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return super.getStateForPlacement(context)
				.setValue(FACING, context.getHorizontalDirection().getOpposite())
				.setValue(OPENING, halfFromFacing(context.getNearestLookingVerticalDirection()));
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return this.shapes.apply(state);
	}

	public static void additionalCollision(BlockState state, PhysicsCollision.Provider.Output output) {
		TriStripBuilder builder = new TriStripBuilder(PieceBlock::pixelsToBlocks).flip();

		CurveGenerator curve = switch (state.getValue(OPENING)) {
			case TOP -> new CurveGenerator(-4, 8, 8, Mth.PI, Mth.HALF_PI * 3, 4);
			case BOTTOM -> new CurveGenerator(-8, -4, 4, Mth.PI, Mth.HALF_PI, 4);
		};

		curve.forEachPoint((x, z) -> builder.then(-4, z, -x).then(4, z, -x));

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

	private static Half halfFromFacing(Direction direction) {
		return switch (direction) {
			case UP -> Half.BOTTOM;
			case DOWN -> Half.TOP;
			default -> throw new IllegalArgumentException("Non-vertical direction: " + direction);
		};
	}
}
