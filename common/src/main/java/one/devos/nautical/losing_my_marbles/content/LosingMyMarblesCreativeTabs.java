package one.devos.nautical.losing_my_marbles.content;

import java.util.function.Consumer;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import one.devos.nautical.losing_my_marbles.LosingMyMarbles;
import one.devos.nautical.losing_my_marbles.content.marble.MarbleItem;
import one.devos.nautical.losing_my_marbles.content.marble.StoredMarble;
import one.devos.nautical.losing_my_marbles.content.marble.data.MarbleType;
import one.devos.nautical.losing_my_marbles.framework.platform.PlatformHelper;

public final class LosingMyMarblesCreativeTabs {
	public static final CreativeModeTab MAIN = register("main", builder -> builder
			.icon(() -> MarbleItem.of(StoredMarble.of(MarbleType.DEFAULT)))
			.displayItems((parameters, output) -> {
				output.accept(LosingMyMarblesBlocks.SUPPORT_PIECE);
				output.accept(LosingMyMarblesBlocks.STRAIGHT_PIECE);
				output.accept(LosingMyMarblesBlocks.CORNER_PIECE);
				output.accept(LosingMyMarblesBlocks.INTERSECTION_PIECE);
				output.accept(LosingMyMarblesBlocks.SLOPE_PIECE);
				output.accept(LosingMyMarblesBlocks.TUBE_PIECE);
				output.accept(LosingMyMarblesBlocks.HALF_PIPE_PIECE);
				output.accept(LosingMyMarblesBlocks.SPLITTER_PIECE);
				output.accept(LosingMyMarblesBlocks.DETECTOR_PIECE);
				output.accept(LosingMyMarblesBlocks.ONE_WAY_GATE_PIECE);
				output.accept(LosingMyMarblesBlocks.POWERED_GATE_PIECE);

				output.accept(LosingMyMarblesBlocks.MARBLE_MAKER);

				HolderLookup.RegistryLookup<MarbleType> typeRegistry = parameters.holders().lookupOrThrow(LosingMyMarblesRegistries.MARBLE_TYPE);
				typeRegistry.listElements().forEach(holder -> {
					ItemStack stack = MarbleItem.of(StoredMarble.of(holder));
					output.accept(stack);
				});
			})
	);

	public static void init() {
	}

	private static CreativeModeTab register(String name, Consumer<CreativeModeTab.Builder> consumer) {
		CreativeModeTab.Builder builder = PlatformHelper.INSTANCE.newCreativeTab();
		builder.title(Component.translatable("itemGroup.losing_my_marbles." + name));
		consumer.accept(builder);
		CreativeModeTab tab = builder.build();

		ResourceLocation id = LosingMyMarbles.id(name);
		return Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, id, tab);
	}
}
