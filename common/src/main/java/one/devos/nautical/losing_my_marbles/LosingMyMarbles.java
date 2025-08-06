package one.devos.nautical.losing_my_marbles;

import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesEntities;

import one.devos.nautical.losing_my_marbles.framework.phys.LevelGeom;

import org.ode4j.ode.OdeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.resources.ResourceLocation;
import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesBlocks;
import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesItems;

public final class LosingMyMarbles {
	public static final String ID = "losing_my_marbles";
	public static final Logger LOGGER = LoggerFactory.getLogger(ID);

	public static void init() {
		LosingMyMarblesItems.init();
		LosingMyMarblesBlocks.init();
		LosingMyMarblesEntities.init();

		OdeHelper.initODE2(0);
		LevelGeom.init();
	}

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(ID, path);
	}
}
