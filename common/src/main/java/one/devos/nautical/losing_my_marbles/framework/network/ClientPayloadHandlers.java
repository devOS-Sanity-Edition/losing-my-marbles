package one.devos.nautical.losing_my_marbles.framework.network;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import one.devos.nautical.losing_my_marbles.LosingMyMarbles;
import one.devos.nautical.losing_my_marbles.content.marble.MarbleEntity;
import one.devos.nautical.losing_my_marbles.content.marble.SpawnMarblePayload;
import one.devos.nautical.losing_my_marbles.content.marble.UpdateMarbleEntityPayload;

public final class ClientPayloadHandlers {
	private ClientPayloadHandlers() {
	}

	public static void updateMarbleEntity(UpdateMarbleEntityPayload payload, LocalPlayer player) {
		Entity entity = player.clientLevel.getEntity(payload.entityId());
		if (!(entity instanceof MarbleEntity marble)) {
			LosingMyMarbles.LOGGER.error("Expected MarbleEntity, got {}", entity);
			return;
		}

		marble.setMarble(payload.marble());
	}

	public static void spawnMarbleEntity(SpawnMarblePayload payload, LocalPlayer player) {
	    payload.basePacket().handle(player.connection);
		Entity entity = player.clientLevel.getEntity(payload.basePacket().getId());
		if (!(entity instanceof MarbleEntity marble)) {
			LosingMyMarbles.LOGGER.error("Failed to spawn MarbleEntity, got {}", entity);
			return;
		}

		marble.setMarble(payload.marble());
	}
}
