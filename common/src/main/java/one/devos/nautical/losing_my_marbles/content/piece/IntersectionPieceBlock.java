package one.devos.nautical.losing_my_marbles.content.piece;

import java.util.function.Function;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class IntersectionPieceBlock extends PieceBlock {
	private static final VoxelShape HOLES = Shapes.or(Block.column(16, 8, 8, 16), Block.column(8, 16, 8, 16));

	private final Function<BlockState, VoxelShape> shapes;

	public IntersectionPieceBlock(Properties properties) {
		super(properties);
		this.shapes = this.getShapeForEachState(shapeFactory(state -> HOLES));
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return this.shapes.apply(state);
	}
}
