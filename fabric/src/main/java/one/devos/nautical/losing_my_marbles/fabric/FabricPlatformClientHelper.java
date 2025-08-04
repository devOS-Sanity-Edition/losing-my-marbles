package one.devos.nautical.losing_my_marbles.fabric;

import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.world.level.block.Block;
import one.devos.nautical.losing_my_marbles.framework.platform.PlatformClientHelper;

public class FabricPlatformClientHelper implements PlatformClientHelper {
	@Override
	public void setBlockRenderLayer(Block block, ChunkSectionLayer layer) {
		BlockRenderLayerMap.putBlock(block, layer);
	}
}
