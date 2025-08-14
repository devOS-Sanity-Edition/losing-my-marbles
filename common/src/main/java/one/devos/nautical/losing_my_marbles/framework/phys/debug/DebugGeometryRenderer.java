package one.devos.nautical.losing_my_marbles.framework.phys.debug;

import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;

public final class DebugGeometryRenderer {
	private static float[] vertices;

	public static void render(PoseStack matrices, Vec3 camPos, MultiBufferSource buffers) {
		if (vertices == null)
			return;

		matrices.pushPose();
		matrices.translate(-camPos.x, -camPos.y, -camPos.z);
		PoseStack.Pose pose = matrices.last();

		VertexConsumer buffer = buffers.getBuffer(RenderType.lines());
		for (int i = 0; i < (vertices.length / 3); i += 3) {
			for (int j = 0; j < 3; j++) {
				int dataStart0 = (i + j) * 3;
				int dataStart1 = (i + ((j + 1) % 3)) * 3;

				float x0 = vertices[dataStart0];
				float y0 = vertices[dataStart0 + 1];
				float z0 = vertices[dataStart0 + 2];
				float x1 = vertices[dataStart1];
				float y1 = vertices[dataStart1 + 1];
				float z1 = vertices[dataStart1 + 2];
				Vector3f normal = new Vector3f(x1 - x0, y1 - y0, z1 - z0).normalize();

				buffer.addVertex(pose, x0, y0, z0).setColor(0xFF00FF00).setNormal(pose, normal);
				buffer.addVertex(pose, x1, y1, z1).setColor(0xFF00FF00).setNormal(pose, normal);
			}
		}

		matrices.popPose();
	}

	public static void handlePayload(DebugGeometryPayload payload, LocalPlayer ignored) {
		vertices = payload.vertices();
	}
}
