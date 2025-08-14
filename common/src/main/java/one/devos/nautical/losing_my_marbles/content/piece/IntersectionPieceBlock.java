package one.devos.nautical.losing_my_marbles.content.piece;

import org.joml.Quaternionf;

import com.github.stephengold.joltjni.Quat;
import com.github.stephengold.joltjni.ShapeRefC;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import one.devos.nautical.losing_my_marbles.framework.phys.terrain.collision.PhysicsCollision;
import one.devos.nautical.losing_my_marbles.framework.phys.util.TriStripBuilder;

public class IntersectionPieceBlock extends PieceBlock {
	private static final VoxelShape SHAPE = PieceBlock.makeShape(Block.column(16, 8, 8, 16), Block.column(8, 16, 8, 16));

	public IntersectionPieceBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	public static void additionalCollision(BlockState ignored, PhysicsCollision.Provider.Output output) {
		// build the bottom right corner
		ShapeRefC corner = new TriStripBuilder(PieceBlock::pixelsToBlocks)
				.then(-1, 0, -8)
				.then(-4, 2, -8)
				.then(-1, 0, -1)
				.then(-4, 2, -4)
				.then(-8, 0, -1)
				.then(-8, 2, -4)
				.build();

		Quat joltRotation = new Quat();
		Quaternionf rotation = new Quaternionf();

		for (int i = 0; i < 4; i++) {
			rotation.rotateY(Mth.DEG_TO_RAD * 90 * i);
			joltRotation.set(rotation.x, rotation.y, rotation.z, rotation.w);
			output.accept(joltRotation, corner);
		}

		corner.close();
	}
}
