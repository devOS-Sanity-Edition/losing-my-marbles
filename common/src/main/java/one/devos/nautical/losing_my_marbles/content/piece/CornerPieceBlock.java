package one.devos.nautical.losing_my_marbles.content.piece;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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
import one.devos.nautical.losing_my_marbles.framework.phys.util.TriStripBuilder;

public class CornerPieceBlock extends PieceBlock {
	public static final EnumProperty<Facing> FACING = EnumProperty.create("facing", Facing.class);

	private static final Map<Facing, CornerShapes> SHAPES = Util.makeEnumMap(Facing.class, facing -> new CornerShapes(
			Shapes.rotate(
					PieceBlock.makeShape(Block.box(4, 8, 0, 12, 16, 12), Block.box(0, 8, 4, 12, 16, 12)),
					facing.rotation
			),
			Shapes.rotate(
					PieceBlock.makeShape(Block.box(0, 8, 0, 12, 16, 12)),
					facing.rotation
			)
	));



	public CornerPieceBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.defaultBlockState().setValue(FACING, Facing.NORTHWEST));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(FACING);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		CornerShapes shapes = SHAPES.get(state.getValue(FACING));
		return context instanceof PhysicsCollisionContext ? shapes.cornerless : shapes.normal;
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		// super method loses the context
		return this.getShape(state, level, pos, context);
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

		final int steps = 4;
		final int centerX = -4;
		final int centerZ = -4;
		final int outerRadius = 8;

		for (int i = 0; i <= steps; i++) {
			float progress = (1f / steps) * i;
			float theta = Mth.lerp(progress, Mth.PI, Mth.HALF_PI);

			float outerX = -Mth.cos(theta) * outerRadius + centerX;
			float outerZ = Mth.sin(theta) * outerRadius + centerZ;

			outerBottom.then(outerX, 0, outerZ).then(outerX, 4, outerZ);
			outerTop.then(outerX, 4, outerZ).then(4, 8, 4);
		}

		float yRot = switch (state.getValue(FACING)) {
			case NORTHWEST -> 0;
			case SOUTHWEST -> 90;
			case SOUTHEAST -> 180;
			case NORTHEAST -> 270;
		};

		Quat rotation = Quat.sEulerAngles(0, Mth.DEG_TO_RAD * yRot, 0);

		try (ShapeRefC outerBottomShape = outerBottom.build(); ShapeRefC outerTopShape = outerTop.build()) {
			output.accept(rotation, outerBottomShape);
			output.accept(rotation, outerTopShape);
		}

		TriStripBuilder inner = new TriStripBuilder(PieceBlock::pixelsToBlocks)
				.then(-4, 0, -8)
				.then(-8, 0, -4)
				.then(-4, 8, -8)
				.then(-8, 8, -4)
				.then(-8, 8, -8);

		try (ShapeRefC innerShape = inner.build()) {
			output.accept(rotation, innerShape);
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

		public static Facing fromDirections(Direction[] directions) {
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

	private record CornerShapes(VoxelShape normal, VoxelShape cornerless) {
	}
}
