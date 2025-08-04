package one.devos.nautical.losing_my_marbles.framework.platform;

import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.world.level.block.Block;

public interface PlatformClientHelper {
	PlatformClientHelper INSTANCE = Services.load(PlatformClientHelper.class);

	void setBlockRenderLayer(Block block, ChunkSectionLayer layer);
}
