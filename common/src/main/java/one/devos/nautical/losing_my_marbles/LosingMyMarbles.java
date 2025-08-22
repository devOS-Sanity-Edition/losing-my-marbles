package one.devos.nautical.losing_my_marbles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.resources.ResourceLocation;
import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesBlockContactEffects;
import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesBlocks;
import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesBounceEffects;
import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesCreativeTabs;
import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesDataComponents;
import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesEntities;
import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesEntityContactEffects;
import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesGameRules;
import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesItems;
import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesMarbleShapeTypes;
import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesMenus;
import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesPayloads;
import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesRecipeBookCategories;
import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesRecipeDisplays;
import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesRecipePropertySets;
import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesRecipeSerializers;
import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesRecipeTypes;
import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesRegistries;
import one.devos.nautical.losing_my_marbles.content.marble.MarbleItemModel;
import one.devos.nautical.losing_my_marbles.content.marble.asset.DyedMarbleTintSource;
import one.devos.nautical.losing_my_marbles.content.marble.asset.texture.MarbleTextures;
import one.devos.nautical.losing_my_marbles.framework.phys.core.JoltIntegration;
import one.devos.nautical.losing_my_marbles.framework.platform.Env;
import one.devos.nautical.losing_my_marbles.framework.platform.PlatformClientHelper;
import one.devos.nautical.losing_my_marbles.framework.platform.PlatformHelper;

public final class LosingMyMarbles {
	public static final String ID = "losing_my_marbles";
	public static final Logger LOGGER = LoggerFactory.getLogger(ID);

	public static void init() {
		JoltIntegration.setup();
		LosingMyMarblesRegistries.init();
		LosingMyMarblesRecipeBookCategories.init();
		LosingMyMarblesItems.init();
		LosingMyMarblesBlocks.init();
		LosingMyMarblesEntities.init();
		LosingMyMarblesCreativeTabs.init();
		LosingMyMarblesPayloads.init();
		LosingMyMarblesDataComponents.init();
		LosingMyMarblesRecipeDisplays.init();
		LosingMyMarblesRecipeSerializers.init();
		LosingMyMarblesRecipeTypes.init();
		LosingMyMarblesRecipePropertySets.init();
		LosingMyMarblesMenus.init();
		LosingMyMarblesGameRules.init();
		LosingMyMarblesMarbleShapeTypes.init();
		LosingMyMarblesBounceEffects.init();
		LosingMyMarblesBlockContactEffects.init();
		LosingMyMarblesEntityContactEffects.init();

		if (PlatformHelper.INSTANCE.getEnvironment() == Env.CLIENT) {
			MarbleTextures.init();
			PlatformClientHelper.INSTANCE.registerItemModel(id("marble"), MarbleItemModel.Unbaked.CODEC);
			PlatformClientHelper.INSTANCE.registerItemTintSource(id("dyed_marble"), DyedMarbleTintSource.MAP_CODEC);

		}
	}

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(ID, path);
	}
}
