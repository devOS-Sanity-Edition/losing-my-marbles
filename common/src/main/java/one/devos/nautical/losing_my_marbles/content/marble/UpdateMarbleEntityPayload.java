package one.devos.nautical.losing_my_marbles.content.marble;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesPayloads;
import one.devos.nautical.losing_my_marbles.content.marble.data.MarbleInstance;

public record UpdateMarbleEntityPayload(int entityId, MarbleInstance marble) implements CustomPacketPayload {
	public static final StreamCodec<RegistryFriendlyByteBuf, UpdateMarbleEntityPayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, UpdateMarbleEntityPayload::entityId,
			one.devos.nautical.losing_my_marbles.content.marble.data.MarbleInstance.STREAM_CODEC, UpdateMarbleEntityPayload::marble,
			UpdateMarbleEntityPayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return LosingMyMarblesPayloads.UPDATE_MARBLE_ENTITY;
	}
}
