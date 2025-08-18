package one.devos.nautical.losing_my_marbles.content.piece;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.github.stephengold.joltjni.Quat;
import com.github.stephengold.joltjni.ShapeRefC;
import com.google.common.collect.Sets;
import com.mojang.math.OctahedralGroup;
import com.mojang.math.Quadrant;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import one.devos.nautical.losing_my_marbles.framework.phys.terrain.collision.PhysicsCollision;
import one.devos.nautical.losing_my_marbles.framework.phys.terrain.collision.PhysicsCollisionContext;
import one.devos.nautical.losing_my_marbles.framework.phys.util.CurveGenerator;
import one.devos.nautical.losing_my_marbles.framework.phys.util.TriStripBuilder;

public class CornerPieceBlock extends PieceBlock {
	public static final EnumProperty<Facing> FACING = EnumProperty.create("facing", Facing.class);

	private static final Map<Facing, VoxelShape> NORMAL_HOLES = Util.makeEnumMap(Facing.class, facing -> Shapes.rotate(
			Shapes.or(
					Block.box(4, 8, 0, 12, 16, 12),
					Block.box(0, 8, 4, 12, 16, 12)
			),
			facing.rotation
	));
	private static final Map<Facing, VoxelShape> CORNERLESS_HOLES = Util.makeEnumMap(Facing.class, facing -> Shapes.rotate(
			Block.box(0, 8, 0, 12, 16, 12), facing.rotation
	));

	private final Function<BlockState, VoxelShape> normalShapes;
	private final Function<BlockState, VoxelShape> cornerlessShapes;

	public CornerPieceBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.defaultBlockState().setValue(FACING, Facing.NORTHWEST));
		this.normalShapes = this.getShapeForEachState(shapeFactory(state -> NORMAL_HOLES.get(state.getValue(FACING))));
		this.cornerlessShapes = this.getShapeForEachState(shapeFactory(state -> CORNERLESS_HOLES.get(state.getValue(FACING))));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(FACING);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		Function<BlockState, VoxelShape> function = context instanceof PhysicsCollisionContext ? this.cornerlessShapes : this.normalShapes;
		return function.apply(state);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return super.getStateForPlacement(context).setValue(FACING, Facing.fromDirections(context.getNearestLookingDirections()));
	}

	@Override
	protected boolean skipRendering(BlockState state, BlockState adjacentBlockState, Direction side) {
		return super.skipRendering(state, adjacentBlockState, side) && state.getValue(FACING).directions.contains(side);
	}

	public static void additionalCollision(BlockState state, PhysicsCollision.Provider.Output output) {
		TriStripBuilder outerBottom = new TriStripBuilder(PieceBlock::pixelsToBlocks).flip();
		TriStripBuilder outerTop = new TriStripBuilder(PieceBlock::pixelsToBlocks).flip();

		new CurveGenerator(-4, -4, 8, Mth.PI, Mth.HALF_PI, 4).forEachPoint((x, z) -> {
			outerBottom.then(x, 0, z).then(x, 4, z);
			outerTop.then(x, 4, z).then(4, 8, 4);
		});

		float yRot = switch (state.getValue(FACING)) {
			case NORTHWEST -> 0;
			case SOUTHWEST -> 90;
			case SOUTHEAST -> 180;
			case NORTHEAST -> 270;
		};

		Quat rotation = Quat.sEulerAngles(0, Mth.DEG_TO_RAD * yRot, 0);

		try (ShapeRefC bottom = outerBottom.build(); ShapeRefC top = outerTop.build()) {
			output.accept(rotation, bottom);
			output.accept(rotation, top);
		}

		TriStripBuilder innerBottom = new TriStripBuilder(PieceBlock::pixelsToBlocks);
		TriStripBuilder innerTop = new TriStripBuilder(PieceBlock::pixelsToBlocks);

		new CurveGenerator(-8, -8, 4, Mth.PI, Mth.HALF_PI, 4).forEachPoint((x, z) -> {
			innerBottom.then(x, 0, z).then(x, 8, z);
			innerTop.then(x, 8, z).then(-8, 8, -8);
		});

		try (ShapeRefC bottom = innerBottom.build(); ShapeRefC top = innerTop.build()) {
			output.accept(rotation, bottom);
			output.accept(rotation, top);
		}
	}

	public enum Facing implements StringRepresentable {
		SOUTHEAST(OctahedralGroup.fromXYAngles(Quadrant.R0, Quadrant.R180), Direction.SOUTH, Direction.EAST),
		SOUTHWEST(OctahedralGroup.fromXYAngles(Quadrant.R0, Quadrant.R270), Direction.SOUTH, Direction.WEST),
		NORTHWEST(OctahedralGroup.fromXYAngles(Quadrant.R0, Quadrant.R0), Direction.NORTH, Direction.WEST),
		NORTHEAST(OctahedralGroup.fromXYAngles(Quadrant.R0, Quadrant.R90), Direction.NORTH, Direction.EAST);

		public final String name;
		public final OctahedralGroup rotation;
		public final Set<Direction> directions;

		Facing(OctahedralGroup rotation, Direction... directions) {
			this.name = this.name().toLowerCase(Locale.ROOT);
			this.rotation = rotation;
			this.directions = Sets.immutableEnumSet(Arrays.asList(directions));
		}

		public static Facing fromDirections(Direction... directions) {
			Direction.AxisDirection zDir = null;
			Direction.AxisDirection xDir = null;

			for (Direction direction : directions) {
				Direction.Axis axis = direction.getAxis();
				Direction.AxisDirection axisDirection = direction.getAxisDirection();
				if (axis == Direction.Axis.Z && zDir == null) {
					zDir = axisDirection.opposite();
				} else if (axis == Direction.Axis.X && xDir == null) {
					xDir = axisDirection.opposite();
				}
			}

			if (zDir == null || xDir == null)
				return NORTHWEST;

			return switch (zDir) {
				case POSITIVE -> switch (xDir) {
					case POSITIVE -> SOUTHEAST;
					case NEGATIVE -> SOUTHWEST;
				};
				case NEGATIVE -> switch (xDir) {
					case NEGATIVE -> NORTHWEST;
					case POSITIVE -> NORTHEAST;
				};
			};
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}
	}
}
