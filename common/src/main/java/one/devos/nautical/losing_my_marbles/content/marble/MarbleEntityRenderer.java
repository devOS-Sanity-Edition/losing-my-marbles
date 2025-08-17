package one.devos.nautical.losing_my_marbles.content.marble;

import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.HitboxRenderState;
import net.minecraft.client.renderer.entity.state.HitboxesRenderState;
import net.minecraft.client.renderer.entity.state.ServerHitboxesRenderState;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesDataComponents;
import one.devos.nautical.losing_my_marbles.content.marble.asset.MarbleAsset;
import one.devos.nautical.losing_my_marbles.content.marble.asset.MarbleAssetManager;
import one.devos.nautical.losing_my_marbles.content.marble.asset.texture.MarbleTexture;

public final class MarbleEntityRenderer extends EntityRenderer<MarbleEntity, MarbleEntityRenderState> {
	private static final RenderType RENDER_TYPE = RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS);
	private static final float SCALE = 12 / 16f;

	private final Function<ResourceLocation, TextureAtlasSprite> spriteLookup;

	public MarbleEntityRenderer(EntityRendererProvider.Context context) {
		super(context);

		TextureAtlas atlas = context.getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS);
		this.spriteLookup = Util.memoize(atlas::getSprite);
	}

	@Override
	public void extractRenderState(MarbleEntity marble, MarbleEntityRenderState state, float partialTicks) {
		super.extractRenderState(marble, state, partialTicks);
		state.distanceTraveled = marble.distanceTraveled();
		state.scale = marble.marble().getOrDefault(LosingMyMarblesDataComponents.SCALE, 1f);
		state.deltaMovement = marble.getDeltaMovement();
		state.marbleAsset = marble.marble().get(LosingMyMarblesDataComponents.ASSET);

		state.serverHitboxesRenderState = getServerHitboxes(marble);
	}

	@Override
	public void render(MarbleEntityRenderState state, PoseStack matrices, MultiBufferSource buffers, int light) {
		super.render(state, matrices, buffers, light);

		MarbleAsset asset = state.marbleAsset != null ? MarbleAssetManager.INSTANCE.get(state.marbleAsset) : null;
		float baseScale = asset != null ? asset.scale() * SCALE : 1;
		float scale = baseScale * state.scale;
		TextureAtlasSprite sprite = this.spriteLookup.apply(
				asset != null ? asset.texture().get(this.createTextureContext(state)) : MissingTextureAtlasSprite.getLocation());

		matrices.pushPose();
		matrices.translate(0, .25 * scale, 0);
		matrices.mulPose(this.entityRenderDispatcher.cameraOrientation());
		matrices.scale(scale, scale, scale);
		matrices.translate(-.5, -.5, 0);
		PoseStack.Pose pose = matrices.last();

		VertexConsumer buffer = buffers.getBuffer(RENDER_TYPE);
		renderVertex(buffer, pose, light, 0, 0, sprite.getU0(), sprite.getV1());
		renderVertex(buffer, pose, light, 1, 0, sprite.getU1(), sprite.getV1());
		renderVertex(buffer, pose, light, 1, 1, sprite.getU1(), sprite.getV0());
		renderVertex(buffer, pose, light, 0, 1, sprite.getU0(), sprite.getV0());

		matrices.popPose();
	}

	private MarbleTexture.Context createTextureContext(MarbleEntityRenderState state) {
		Vec3 cameraPosition = this.entityRenderDispatcher.camera.getPosition();
		return new MarbleTexture.Context(
				state.distanceTraveled,
				state.deltaMovement,
				new Vec3(state.x - cameraPosition.x, state.y - cameraPosition.y, state.z - cameraPosition.z)
		);
	}

	private static void renderVertex(VertexConsumer buffer, PoseStack.Pose pose, int light, float x, float y, float textureU, float textureV) {
		buffer.addVertex(pose, x, y, 0)
				.setColor(0xFFFFFFFF)
				.setUv(textureU, textureV)
				.setOverlay(OverlayTexture.NO_OVERLAY)
				.setLight(light)
				.setNormal(0, 1, 0);
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
