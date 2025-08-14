package one.devos.nautical.losing_my_marbles.framework.phys.debug;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import one.devos.nautical.losing_my_marbles.content.packet.DebugGeometryPayload;

public final class DebugGeometryRenderer {
	private static float[] vertices;

	public static void render(PoseStack matrices, MultiBufferSource buffers) {
		if (vertices == null)
			return;

		// TODO
	}

	public static void handlePayload(DebugGeometryPayload payload, LocalPlayer ignored) {
		vertices = payload.vertices();
	}
}
