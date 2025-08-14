package one.devos.nautical.losing_my_marbles.framework.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public interface ServerPlayPayloadHandler<T extends CustomPacketPayload> {
	void handle(T payload, ServerPlayer player);
}
