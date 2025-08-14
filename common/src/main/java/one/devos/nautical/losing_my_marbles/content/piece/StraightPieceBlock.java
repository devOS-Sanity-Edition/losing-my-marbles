package one.devos.nautical.losing_my_marbles.content.piece;

import java.util.Map;

import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import com.github.stephengold.joltjni.Quat;
import com.github.stephengold.joltjni.ShapeRefC;
import com.github.stephengold.joltjni.Vec3;

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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import one.devos.nautical.losing_my_marbles.framework.phys.terrain.collision.PhysicsCollision;
import one.devos.nautical.losing_my_marbles.framework.phys.util.PhysUtils;

public class StraightPieceBlock extends PieceBlock {
	public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;

	private static final Map<Direction.Axis, VoxelShape> SHAPES = Shapes.rotateHorizontalAxis(PieceBlock.makeShape(Block.column(8, 16, 8, 16)));

	public StraightPieceBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.defaultBlockState().setValue(AXIS, Direction.Axis.X));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(AXIS);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPES.get(state.getValue(AXIS));
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(AXIS, context.getHorizontalDirection().getAxis());
	}

	@Override
	protected boolean skipRendering(BlockState state, BlockState adjacentBlockState, Direction side) {
		return super.skipRendering(state, adjacentBlockState, side) && side.getAxis() == state.getValue(AXIS);
	}

	public static void additionalCollision(BlockState state, PhysicsCollision.Provider.Output output) {
		Quaternionf rotation = new Quaternionf();

		if (state.getValue(AXIS) == Direction.Axis.X) {
			rotation.rotateY(Mth.DEG_TO_RAD * 90);
		}

		Quat joltRotation = new Quat(rotation.x, rotation.y, rotation.z, rotation.w);

		ShapeRefC leftSlope = PhysUtils.quad(
				pixelsToBlocks(new Vec3(1, 0, -8)),
				pixelsToBlocks(new Vec3(1, 0, 8)),
				pixelsToBlocks(new Vec3(4, 2, 8)),
				pixelsToBlocks(new Vec3(4, 2, -8))
		);

		output.accept(joltRotation, leftSlope);

		rotation.rotateY(Mth.DEG_TO_RAD * 180);
		joltRotation.set(rotation.x, rotation.y, rotation.z, rotation.w);

		output.accept(joltRotation, leftSlope);

		leftSlope.close();
	}

	private static Vec3 rotate(Direction.Axis axis, Vec3 vec) {
		if (axis == Direction.Axis.X) {
			float x = vec.getX();
			vec.setX(vec.getZ());
			vec.setZ(x);
		}

		return vec;
	}
}
