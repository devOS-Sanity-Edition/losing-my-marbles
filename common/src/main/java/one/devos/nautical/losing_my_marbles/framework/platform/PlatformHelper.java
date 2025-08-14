package one.devos.nautical.losing_my_marbles.framework.platform;

import java.nio.file.Path;
import java.util.Optional;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import one.devos.nautical.losing_my_marbles.framework.network.ClientPlayPayloadHandler;
import one.devos.nautical.losing_my_marbles.framework.network.ServerPlayPayloadHandler;
import one.devos.nautical.losing_my_marbles.framework.phys.PhysicsEnvironment;

public interface PlatformHelper {
	PlatformHelper INSTANCE = Services.load(PlatformHelper.class);

	Env getEnvironment();

	Path getGameDir();

	Optional<Path> findPath(String name);

	PhysicsEnvironment getPhysicsEnvironment(ServerLevel level);

	<T extends CustomPacketPayload> CustomPacketPayload.TypeAndCodec<? super RegistryFriendlyByteBuf, T> registerPlayPayloadC2S(
			CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec
	);

	<T extends CustomPacketPayload> CustomPacketPayload.TypeAndCodec<? super RegistryFriendlyByteBuf, T> registerPlayPayloadS2C(
			CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec
	);

	/**
	 * @see PlatformClientHelper#registerPlayPayloadHandler(CustomPacketPayload.Type, ClientPlayPayloadHandler)
	 */
	<T extends CustomPacketPayload> void registerPlayPayloadHandler(CustomPacketPayload.Type<T> type, ServerPlayPayloadHandler<T> handler);
}
