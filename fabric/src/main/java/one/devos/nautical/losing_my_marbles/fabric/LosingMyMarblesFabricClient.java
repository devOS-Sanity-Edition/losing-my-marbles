package one.devos.nautical.losing_my_marbles.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.server.packs.PackType;
import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesMenus;
import one.devos.nautical.losing_my_marbles.content.marble.asset.MarbleAssetManager;
import one.devos.nautical.losing_my_marbles.content.marble.maker.MarbleMakerScreen;
import one.devos.nautical.losing_my_marbles.framework.phys.debug.DebugGeometryRenderer;

public final class LosingMyMarblesFabricClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		WorldRenderEvents.AFTER_ENTITIES.register(context ->
				DebugGeometryRenderer.render(context.matrixStack(), context.camera().position(), context.consumers()));
		ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener((IdentifiableResourceReloadListener) (Object) MarbleAssetManager.INSTANCE);
		MenuScreens.register(LosingMyMarblesMenus.MARBLE_MAKER, MarbleMakerScreen::new);
	}
}
