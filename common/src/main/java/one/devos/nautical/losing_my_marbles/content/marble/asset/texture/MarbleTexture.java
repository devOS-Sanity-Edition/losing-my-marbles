package one.devos.nautical.losing_my_marbles.content.marble.asset.texture;

import com.mojang.serialization.MapCodec;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public interface MarbleTexture {
	ResourceLocation get(Context context);

	MapCodec<? extends MarbleTexture> type();

	record Context(double distanceTraveled, Vec3 deltaMovement, Vec3 relativeViewPosition) {
	}
}
