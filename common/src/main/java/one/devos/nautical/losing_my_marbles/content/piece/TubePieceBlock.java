package one.devos.nautical.losing_my_marbles.content.piece;

import java.util.function.Function;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class TubePieceBlock extends PieceBlock {
	public static final VoxelShape HOLE = box(4, 0, 4, 12, 16, 12);

	private final Function<BlockState, VoxelShape> shapes;

	public TubePieceBlock(Properties properties) {
		super(properties);
		this.shapes = this.getShapeForEachState(shapeFactory(state -> HOLE));
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return this.shapes.apply(state);
	}
}
