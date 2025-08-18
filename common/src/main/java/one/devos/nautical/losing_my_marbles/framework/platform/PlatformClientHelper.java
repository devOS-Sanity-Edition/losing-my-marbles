package one.devos.nautical.losing_my_marbles.framework.platform;

import com.mojang.serialization.MapCodec;

import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.gui.screens.recipebook.GhostSlots;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.block.Block;
import one.devos.nautical.losing_my_marbles.framework.network.ClientPlayPayloadHandler;

public interface PlatformClientHelper {
	PlatformClientHelper INSTANCE = Services.load(PlatformClientHelper.class);

	void setBlockRenderLayer(ChunkSectionLayer layer, Block... blocks);

	<T extends Entity> void registerEntityRenderer(EntityType<T> type, EntityRendererProvider<T> provider);

	<T extends CustomPacketPayload> void registerPlayPayloadHandler(CustomPacketPayload.Type<T> type, ClientPlayPayloadHandler<T> handler);

	void registerItemModel(ResourceLocation id, MapCodec<? extends ItemModel.Unbaked> type);

	void registerItemTintSource(ResourceLocation id, MapCodec<? extends ItemTintSource> codec);

	void setGhostSlotsResult(GhostSlots ghostSlots, Slot slot, ContextMap contextMap, SlotDisplay slotDisplay);

	void setGhostSlotsInput(GhostSlots ghostSlots, Slot slot, ContextMap contextMap, SlotDisplay slotDisplay);
}
