package one.devos.nautical.losing_my_marbles.content.marble;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.phys.Vec3;
import one.devos.nautical.losing_my_marbles.content.marble.asset.MarbleAsset;

public final class MarbleEntityRenderState extends EntityRenderState {
	public double distanceTraveled;
	public float scale;
	public Vec3 deltaMovement;

	public int color;
	@Nullable
	public ResourceKey<MarbleAsset> marbleAsset;
}
