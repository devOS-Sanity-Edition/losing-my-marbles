package one.devos.nautical.losing_my_marbles.content.piece;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class IntersectionPieceBlock extends PieceBlock {
	private static final VoxelShape SHAPE = PieceBlock.makeShape(Block.column(16, 8, 8, 16), Block.column(8, 16, 8, 16));

	public IntersectionPieceBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}
}
