package one.devos.nautical.losing_my_marbles.framework.phys.debug;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesPayloads;
import one.devos.nautical.losing_my_marbles.framework.network.LosingMyMarblesStreamCodecs;

public record DebugGeometryPayload(float[] vertices) implements CustomPacketPayload {
	public static final StreamCodec<ByteBuf, DebugGeometryPayload> CODEC = StreamCodec.composite(
			LosingMyMarblesStreamCodecs.FLOAT_ARRAY, DebugGeometryPayload::vertices,
			DebugGeometryPayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return LosingMyMarblesPayloads.DEBUG_GEOMETRY;
	}
}
