package one.devos.nautical.losing_my_marbles.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import one.devos.nautical.losing_my_marbles.LosingMyMarbles;

import one.devos.nautical.losing_my_marbles.framework.phys.PhysicsEnvironment;
import one.devos.nautical.losing_my_marbles.framework.phys.PhysicsEventListeners;

public final class LosingMyMarblesFabric implements ModInitializer {
	public static final AttachmentType<PhysicsEnvironment> PHYSICS_ENV_ATTACHMENT = AttachmentRegistry.create(
			LosingMyMarbles.id("physics_environment")
	);

	@Override
	public void onInitialize() {
		LosingMyMarbles.init();
		ServerTickEvents.START_WORLD_TICK.register(PhysicsEventListeners::tick);
		ServerEntityEvents.ENTITY_LOAD.register(PhysicsEventListeners::entityLoaded);
		ServerEntityEvents.ENTITY_UNLOAD.register(PhysicsEventListeners::entityUnloaded);
	}
}
