package one.devos.nautical.losing_my_marbles.content.marble;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.HitboxRenderState;
import net.minecraft.client.renderer.entity.state.HitboxesRenderState;
import net.minecraft.client.renderer.entity.state.ServerHitboxesRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;

import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

public final class MarbleEntityRenderer extends EntityRenderer<MarbleEntity, MarbleEntityRenderState> {
	public MarbleEntityRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public void extractRenderState(MarbleEntity marble, MarbleEntityRenderState state, float partialTicks) {
		super.extractRenderState(marble, state, partialTicks);
		state.serverHitboxesRenderState = getServerHitboxes(marble);
	}

	@Override
	public void render(MarbleEntityRenderState state, PoseStack matrices, MultiBufferSource vertices, int light) {
		super.render(state, matrices, vertices, light);

		ItemStack stack = new ItemStack(Items.SNOWBALL);

		matrices.pushPose();

		matrices.translate(0, 0.05, 0);
		matrices.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());

		Minecraft.getInstance().getItemRenderer().renderStatic(
				stack, ItemDisplayContext.GROUND, light, OverlayTexture.NO_OVERLAY, matrices, vertices, null, 0
		);

		matrices.popPose();
	}

	@Override
	public MarbleEntityRenderState createRenderState() {
		return new MarbleEntityRenderState();
	}

	@Nullable
	public static ServerHitboxesRenderState getServerHitboxes(Entity clientEntity) {
		IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
		if (server == null)
			return null;

		ServerLevel serverLevel = server.getLevel(clientEntity.level().dimension());
		if (serverLevel == null)
			return null;

		Entity entity = serverLevel.getEntity(clientEntity.getId());
		if (entity == null)
			return null;

		Vec3 vel = entity.getDeltaMovement();
		Vec3 view = entity.getLookAngle();
		AABB bounds = entity.getBoundingBox().move(entity.position().scale(-1));

		return new ServerHitboxesRenderState(
				false,
				entity.getX(), entity.getY(), entity.getZ(),
				vel.x, vel.y, vel.z,
				entity.getEyeHeight(),
				new HitboxesRenderState(
						view.x, view.y, view.z,
						ImmutableList.of(new HitboxRenderState(
								bounds.minX, bounds.minY, bounds.minZ,
								bounds.maxX, bounds.maxY, bounds.maxZ,
								0, 1, 0
						))
				)
		);
	}
}
