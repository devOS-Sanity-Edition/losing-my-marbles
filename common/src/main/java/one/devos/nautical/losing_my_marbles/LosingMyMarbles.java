package one.devos.nautical.losing_my_marbles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.resources.ResourceLocation;
import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesBlocks;
import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesBounceEffects;
import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesCreativeTabs;
import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesDataComponents;
import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesEntities;
import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesEntityContactEffects;
import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesItems;
import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesMarbleShapeTypes;
import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesPayloads;
import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesRegistries;
import one.devos.nautical.losing_my_marbles.framework.phys.core.JoltIntegration;

public final class LosingMyMarbles {
	public static final String ID = "losing_my_marbles";
	public static final Logger LOGGER = LoggerFactory.getLogger(ID);

	public static void init() {
		JoltIntegration.setup();
		LosingMyMarblesRegistries.init();
		LosingMyMarblesItems.init();
		LosingMyMarblesBlocks.init();
		LosingMyMarblesEntities.init();
		LosingMyMarblesCreativeTabs.init();
		LosingMyMarblesPayloads.init();
		LosingMyMarblesDataComponents.init();
		LosingMyMarblesMarbleShapeTypes.init();
		LosingMyMarblesBounceEffects.init();
		LosingMyMarblesEntityContactEffects.init();
	}

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(ID, path);
	}
}
