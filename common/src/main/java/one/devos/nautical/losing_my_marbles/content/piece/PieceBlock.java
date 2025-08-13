package one.devos.nautical.losing_my_marbles.content.piece;

import java.util.Arrays;

import com.github.stephengold.joltjni.Vec3;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class PieceBlock extends TransparentBlock {
	public static final EnumProperty<DyeColor> COLOR = EnumProperty.create("color", DyeColor.class);

	protected static final VoxelShape LEG_EXCLUSION_SHAPE = Shapes.or(Block.column(16, 8, 0, 3), Block.column(8, 16, 0, 3));

	protected static VoxelShape makeShape(VoxelShape... shapes) {
		return Shapes.join(Shapes.block(), Arrays.stream(shapes).reduce(LEG_EXCLUSION_SHAPE, Shapes::or), BooleanOp.ONLY_FIRST);
	}

	protected PieceBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.defaultBlockState().setValue(COLOR, DyeColor.WHITE));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(COLOR);
	}

	@Override
	protected boolean skipRendering(BlockState state, BlockState adjacentBlockState, Direction side) {
		return adjacentBlockState.getBlock() instanceof PieceBlock;
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (stack.getItem() instanceof DyeItem dye && player.getAbilities().mayBuild) {
			DyeColor color = dye.getDyeColor();
			if (color != state.getValue(COLOR)) {
				level.playSound(player, pos, SoundEvents.BRUSH_GENERIC, SoundSource.BLOCKS);
				if (!level.isClientSide) {
					level.setBlockAndUpdate(pos, state.setValue(COLOR, color));
					level.gameEvent(GameEvent.BLOCK_CHANGE, pos, new GameEvent.Context(player, state));
				}
				return InteractionResult.SUCCESS;
			}
		}
		return InteractionResult.PASS;
	}

	public static Vec3 pixelsToBlocks(Vec3 vec) {
		vec.scaleInPlace(1 / 16f);
		return vec;
	}

	public static float pixelsToBlocks(float pixels) {
		return pixels / 16;
	}
}
