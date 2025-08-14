package one.devos.nautical.losing_my_marbles.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import one.devos.nautical.losing_my_marbles.framework.phys.debug.DebugGeometryRenderer;

public final class LosingMyMarblesFabricClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		WorldRenderEvents.AFTER_ENTITIES.register(context -> DebugGeometryRenderer.render(context.matrixStack(), context.consumers()));
	}
}
