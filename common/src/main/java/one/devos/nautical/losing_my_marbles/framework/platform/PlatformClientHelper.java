package one.devos.nautical.losing_my_marbles.framework.platform;

import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;

public interface PlatformClientHelper {
	PlatformClientHelper INSTANCE = Services.load(PlatformClientHelper.class);

	void setBlockRenderLayer(ChunkSectionLayer layer, Block... blocks);

	<T extends Entity> void registerEntityRenderer(EntityType<T> type, EntityRendererProvider<T> provider);
}
