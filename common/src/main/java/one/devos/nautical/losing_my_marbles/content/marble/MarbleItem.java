package one.devos.nautical.losing_my_marbles.content.marble;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesDataComponents;

public final class MarbleItem extends Item {
	public MarbleItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		ItemStack stack = context.getItemInHand();
		StoredMarble marble = stack.get(LosingMyMarblesDataComponents.MARBLE);
		if (marble == null) {
			return InteractionResult.FAIL;
		}

		BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
		Level level = context.getLevel();

		if (!level.noCollision(new AABB(pos)))
			return InteractionResult.FAIL;

		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}

		MarbleEntity entity = new MarbleEntity(level, marble.get(level.registryAccess()));
		entity.setPos(Vec3.atCenterOf(pos));
		level.addFreshEntity(entity);

		return InteractionResult.SUCCESS_SERVER;
	}
}
