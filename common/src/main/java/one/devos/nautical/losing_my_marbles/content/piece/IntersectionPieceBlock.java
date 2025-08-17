package one.devos.nautical.losing_my_marbles.content.piece;

import java.util.function.Function;

import org.joml.Quaternionf;

import com.github.stephengold.joltjni.Quat;
import com.github.stephengold.joltjni.ShapeRefC;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import one.devos.nautical.losing_my_marbles.framework.phys.terrain.collision.PhysicsCollision;
import one.devos.nautical.losing_my_marbles.framework.phys.terrain.collision.PhysicsCollisionContext;
import one.devos.nautical.losing_my_marbles.framework.phys.util.CurveGenerator;
import one.devos.nautical.losing_my_marbles.framework.phys.util.TriStripBuilder;

public class IntersectionPieceBlock extends PieceBlock {
	private static final VoxelShape TOP_HALF = box(0, 8, 0, 16, 16, 16);
	private static final VoxelShape HOLES = Shapes.or(Block.column(16, 8, 8, 16), Block.column(8, 16, 8, 16));

	private final Function<BlockState, VoxelShape> normalShapes;
	private final Function<BlockState, VoxelShape> physicsShapes;

	public IntersectionPieceBlock(Properties properties) {
		super(properties);
		this.normalShapes = this.getShapeForEachState(shapeFactory(state -> HOLES));
		this.physicsShapes = this.getShapeForEachState(shapeFactory(state -> TOP_HALF));
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		Function<BlockState, VoxelShape> function = context instanceof PhysicsCollisionContext ? this.physicsShapes : this.normalShapes;
		return function.apply(state);
	}

	public static void additionalCollision(BlockState state, PhysicsCollision.Provider.Output output) {
		TriStripBuilder wall = new TriStripBuilder(PieceBlock::pixelsToBlocks);
		TriStripBuilder top = new TriStripBuilder(PieceBlock::pixelsToBlocks);

		new CurveGenerator(-8, -8, 4, Mth.PI, Mth.HALF_PI, 4).forEachPoint((x, z) -> {
			wall.then(x, 0, z).then(x, 8, z);
			top.then(x, 8, z).then(-8, 8, -8);
		});

		Quat joltRotation = new Quat();
		Quaternionf rotation = new Quaternionf();

		try (ShapeRefC wallShape = wall.build(); ShapeRefC topShape = top.build()) {
			for (int i = 0; i < 4; i++) {
				rotation.rotateY(Mth.DEG_TO_RAD * 90);
				joltRotation.set(rotation.x, rotation.y, rotation.z, rotation.w);

				output.accept(joltRotation, wallShape);
				output.accept(joltRotation, topShape);
			}
		}
	}
}
