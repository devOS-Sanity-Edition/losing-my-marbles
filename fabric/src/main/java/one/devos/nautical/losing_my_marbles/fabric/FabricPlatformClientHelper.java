package one.devos.nautical.losing_my_marbles.fabric;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import one.devos.nautical.losing_my_marbles.framework.network.ClientPlayPayloadHandler;
import one.devos.nautical.losing_my_marbles.framework.platform.PlatformClientHelper;

public class FabricPlatformClientHelper implements PlatformClientHelper {
	@Override
	public void setBlockRenderLayer(ChunkSectionLayer layer, Block... blocks) {
		BlockRenderLayerMap.putBlocks(layer, blocks);
	}

	@Override
	public <T extends Entity> void registerEntityRenderer(EntityType<T> type, EntityRendererProvider<T> provider) {
		EntityRendererRegistry.register(type, provider);
	}

	@Override
	public <T extends CustomPacketPayload> void registerPlayPayloadHandler(CustomPacketPayload.Type<T> type, ClientPlayPayloadHandler<T> handler) {
		ClientPlayNetworking.registerGlobalReceiver(type, (payload, context) -> handler.handle(payload, context.player()));
	}
}
