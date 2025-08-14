package one.devos.nautical.losing_my_marbles.fabric;

import java.nio.file.Path;
import java.util.Optional;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import one.devos.nautical.losing_my_marbles.LosingMyMarbles;
import one.devos.nautical.losing_my_marbles.framework.network.ServerPlayPayloadHandler;
import one.devos.nautical.losing_my_marbles.framework.phys.PhysicsEnvironment;
import one.devos.nautical.losing_my_marbles.framework.platform.Env;
import one.devos.nautical.losing_my_marbles.framework.platform.PlatformHelper;

public class FabricPlatformHelper implements PlatformHelper {
	@Override
	public Env getEnvironment() {
		return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT ? Env.CLIENT : Env.SERVER;
	}

	@Override
	public Path getGameDir() {
		return FabricLoader.getInstance().getGameDir();
	}

	@Override
	public Optional<Path> findPath(String name) {
		return FabricLoader.getInstance().getModContainer(LosingMyMarbles.ID).orElseThrow().findPath(name);
	}

	@Override
	public PhysicsEnvironment getPhysicsEnvironment(ServerLevel level) {
		return level.getAttachedOrCreate(LosingMyMarblesFabric.PHYSICS_ENV_ATTACHMENT, () -> new PhysicsEnvironment(level));
	}

	@Override
	public <T extends CustomPacketPayload> CustomPacketPayload.TypeAndCodec<? super RegistryFriendlyByteBuf, T> registerPlayPayloadC2S(
			CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
		return PayloadTypeRegistry.playC2S().register(type, codec);
	}

	@Override
	public <T extends CustomPacketPayload> CustomPacketPayload.TypeAndCodec<? super RegistryFriendlyByteBuf, T> registerPlayPayloadS2C(
			CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
		return PayloadTypeRegistry.playS2C().register(type, codec);
	}

	@Override
	public <T extends CustomPacketPayload> void registerPlayPayloadHandler(CustomPacketPayload.Type<T> type, ServerPlayPayloadHandler<T> handler) {
		ServerPlayNetworking.registerGlobalReceiver(type, (payload, context) -> handler.handle(payload, context.player()));
	}
}
