package one.devos.nautical.losing_my_marbles.content.command;

import static net.minecraft.commands.Commands.literal;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.server.level.ServerPlayer;
import one.devos.nautical.losing_my_marbles.content.packet.DebugGeometryPayload;
import one.devos.nautical.losing_my_marbles.framework.phys.PhysicsEnvironment;

public final class RequestGeometryCommand {
	public static final Component NO_GEOMETRY = Component.literal("No geometry to send.");
	public static final Component SENT_GEOMETRY = Component.literal("Debug geometry has been sent.");

	public static LiteralArgumentBuilder<CommandSourceStack> build() {
		return literal("request_geometry")
				.requires(source -> source.hasPermission(2))
				.executes(context -> {
					CommandSourceStack source = context.getSource();
					ServerPlayer player = source.getPlayerOrException();

					PhysicsEnvironment environment = PhysicsEnvironment.get(player.level());
					List<FloatBuffer> vertexBuffers = new ArrayList<>();
					environment.collectDebugGeometry(player.getBoundingBox(), vertexBuffers::add);

					if (vertexBuffers.isEmpty()) {
						source.sendSuccess(() -> NO_GEOMETRY, false);
						return 0;
					}

					int totalVertices = vertexBuffers.stream().mapToInt(Buffer::capacity).sum();
					float[] array = new float[totalVertices];
					int offset = 0;
					for (FloatBuffer buffer : vertexBuffers) {
						buffer.get(array, offset, buffer.capacity());
						offset += buffer.capacity();
					}

					player.connection.send(new ClientboundCustomPayloadPacket(new DebugGeometryPayload(array)));
					source.sendSuccess(() -> SENT_GEOMETRY, false);
					return 1;
				});
	}
}
