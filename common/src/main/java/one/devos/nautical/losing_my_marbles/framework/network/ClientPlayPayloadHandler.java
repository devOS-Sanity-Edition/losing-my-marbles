package one.devos.nautical.losing_my_marbles.framework.network;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface ClientPlayPayloadHandler<T extends CustomPacketPayload> {
	void handle(T payload, LocalPlayer player);
}
