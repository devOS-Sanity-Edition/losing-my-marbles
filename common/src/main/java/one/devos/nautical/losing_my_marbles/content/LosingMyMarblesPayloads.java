package one.devos.nautical.losing_my_marbles.content;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import one.devos.nautical.losing_my_marbles.LosingMyMarbles;
import one.devos.nautical.losing_my_marbles.content.packet.DebugGeometryPayload;
import one.devos.nautical.losing_my_marbles.framework.network.ClientPlayPayloadHandler;
import one.devos.nautical.losing_my_marbles.framework.network.ServerPlayPayloadHandler;
import one.devos.nautical.losing_my_marbles.framework.phys.debug.DebugGeometryRenderer;
import one.devos.nautical.losing_my_marbles.framework.platform.Env;
import one.devos.nautical.losing_my_marbles.framework.platform.PlatformClientHelper;
import one.devos.nautical.losing_my_marbles.framework.platform.PlatformHelper;

public final class LosingMyMarblesPayloads {
	public static final CustomPacketPayload.Type<DebugGeometryPayload> DEBUG_GEOMETRY = registerS2C("debug_geometry", DebugGeometryPayload.CODEC);

	public static void init() {
		if (PlatformHelper.INSTANCE.getEnvironment() != Env.CLIENT)
			return;

		registerHandlerS2C(DEBUG_GEOMETRY, DebugGeometryRenderer::handlePayload);
	}

	private static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> registerC2S(
			String name, StreamCodec<? super RegistryFriendlyByteBuf, T> codec, ServerPlayPayloadHandler<T> handler) {
		CustomPacketPayload.Type<T> type = new CustomPacketPayload.Type<>(LosingMyMarbles.id(name));
		PlatformHelper.INSTANCE.registerPlayPayloadC2S(type, codec);
		PlatformHelper.INSTANCE.registerPlayPayloadHandler(type, handler);
		return type;
	}

	private static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> registerS2C(
			String name, StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
		CustomPacketPayload.Type<T> type = new CustomPacketPayload.Type<>(LosingMyMarbles.id(name));
		PlatformHelper.INSTANCE.registerPlayPayloadS2C(type, codec);
		return type;
	}

	private static <T extends CustomPacketPayload> void registerHandlerS2C(CustomPacketPayload.Type<T> type, ClientPlayPayloadHandler<T> handler) {
		PlatformClientHelper.INSTANCE.registerPlayPayloadHandler(type, handler);
	}
}
