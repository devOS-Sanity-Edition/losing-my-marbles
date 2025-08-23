package one.devos.nautical.losing_my_marbles.content.marble;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesDataComponents;
import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesEntities;
import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesItems;
import one.devos.nautical.losing_my_marbles.content.marble.data.MarbleInstance;

// placement and dispense logic based on spawn eggs
public final class MarbleItem extends Item {
	public static final double DISPENSED_VELOCITY = 0.4;

	public static final DispenseItemBehavior DISPENSE_BEHAVIOR = new DefaultDispenseItemBehavior() {
		@Override
		public ItemStack execute(BlockSource source, ItemStack stack) {
			Direction facing = source.state().getValue(DispenserBlock.FACING);

			Consumer<MarbleEntity> config = EntityType.appendDefaultStackConfig(entity -> {
				StoredMarble marble = stack.get(LosingMyMarblesDataComponents.MARBLE);
				if (marble != null) {
					marble.get(entity.registryAccess()).ifPresent(entity::setMarble);
				}

				nudge(entity);

				if (facing.getAxis().isHorizontal()) {
					Vec3 vel = facing.getUnitVec3().scale(DISPENSED_VELOCITY);
					entity.setDeltaMovement(vel);
				}
			}, source.level(), stack, null);

			try {
				BlockPos pos = source.pos().relative(facing);
				boolean offset = facing != Direction.UP;
				LosingMyMarblesEntities.MARBLE.spawn(source.level(), config, pos, EntitySpawnReason.DISPENSER, offset, false);
			} catch (Exception var6) {
				LOGGER.error("Error while dispensing marble from dispenser at {}", source.pos(), var6);
				return ItemStack.EMPTY;
			}

			stack.shrink(1);
			source.level().gameEvent(null, GameEvent.ENTITY_PLACE, source.pos());
			return stack;
		}
	};

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

		Level level = context.getLevel();

		Optional<MarbleInstance> instance = marble.get(level.registryAccess());
		if (instance.isEmpty()) {
			return InteractionResult.FAIL;
		}

		if (!(level instanceof ServerLevel serverLevel)) {
			return InteractionResult.SUCCESS;
		}

		Direction face = context.getClickedFace();
		BlockPos pos = context.getClickedPos();
		BlockState state = level.getBlockState(pos);
		Player player = context.getPlayer();

		BlockPos actualPos = state.getCollisionShape(level, pos).isEmpty() ? pos : pos.relative(face);
		boolean offset = !Objects.equals(pos, actualPos) && face == Direction.UP;

		Consumer<MarbleEntity> config = EntityType.appendDefaultStackConfig(entity -> {
			entity.setMarble(instance.get());
			entity.setOwner(player);
			nudge(entity);
		}, level, stack, player);

		MarbleEntity spawned = LosingMyMarblesEntities.MARBLE.spawn(
				serverLevel, config, actualPos, EntitySpawnReason.SPAWN_ITEM_USE, true, offset
		);

		if (spawned != null) {
			stack.shrink(1);
			level.gameEvent(player, GameEvent.ENTITY_PLACE, pos);
		}

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

	private static void nudge(MarbleEntity entity) {
		RandomSource random = entity.getRandom();
		entity.setPos(entity.position().add(
				(random.nextDouble() - 0.5) / 10,
				0,
				(random.nextDouble() - 0.5) / 10
		));
	}
}
