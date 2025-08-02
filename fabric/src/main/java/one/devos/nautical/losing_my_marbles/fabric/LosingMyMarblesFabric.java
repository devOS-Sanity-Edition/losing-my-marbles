package one.devos.nautical.losing_my_marbles.fabric;

import net.fabricmc.api.ModInitializer;
import one.devos.nautical.losing_my_marbles.LosingMyMarbles;

public final class LosingMyMarblesFabric implements ModInitializer {
	@Override
	public void onInitialize() {
		LosingMyMarbles.init();
	}
}
