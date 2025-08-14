package one.devos.nautical.losing_my_marbles.content.marble;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesPayloads;
import one.devos.nautical.losing_my_marbles.content.marble.data.MarbleInstance;

public record SpawnMarblePayload(ClientboundAddEntityPacket basePacket, MarbleInstance marble) implements CustomPacketPayload {
	public static final StreamCodec<RegistryFriendlyByteBuf, SpawnMarblePayload> CODEC = StreamCodec.composite(
			ClientboundAddEntityPacket.STREAM_CODEC, SpawnMarblePayload::basePacket,
			MarbleInstance.STREAM_CODEC, SpawnMarblePayload::marble,
			SpawnMarblePayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return LosingMyMarblesPayloads.SPAWN_MARBLE;
	}
}
