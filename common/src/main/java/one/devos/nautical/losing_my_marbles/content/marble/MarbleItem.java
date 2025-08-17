package one.devos.nautical.losing_my_marbles.content.marble;

import java.util.Optional;
import java.util.function.Consumer;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesDataComponents;
import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesItems;
import one.devos.nautical.losing_my_marbles.content.marble.data.MarbleInstance;

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

		Optional<MarbleInstance> instance = marble.get(level.registryAccess());
		if (instance.isEmpty()) {
			return InteractionResult.FAIL;
		}

		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}

		MarbleEntity entity = new MarbleEntity(level, instance.get(), context.getPlayer());
		entity.setPos(Vec3.atCenterOf(pos));
		level.addFreshEntity(entity);

		stack.shrink(1);
		return InteractionResult.SUCCESS_SERVER;
	}

	@Override
	@SuppressWarnings("deprecation") // this method
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> output, TooltipFlag flag) {
		stack.addToTooltip(LosingMyMarblesDataComponents.MARBLE, context, display, output, flag);
	}

	public static ItemStack of(StoredMarble marble) {
		ItemStack stack = new ItemStack(LosingMyMarblesItems.MARBLE);
		stack.set(LosingMyMarblesDataComponents.MARBLE, marble);
		return stack;
	}
}
