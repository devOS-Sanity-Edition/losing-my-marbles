package one.devos.nautical.losing_my_marbles.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import one.devos.nautical.losing_my_marbles.framework.phys.PhysicsEventListeners;

public final class LosingMyMarblesFabricClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientTickEvents.START_WORLD_TICK.register(PhysicsEventListeners::tick);
		ClientEntityEvents.ENTITY_LOAD.register(PhysicsEventListeners::entityLoaded);
		ClientEntityEvents.ENTITY_UNLOAD.register(PhysicsEventListeners::entityUnloaded);
	}
}
