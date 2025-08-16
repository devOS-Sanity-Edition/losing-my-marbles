package one.devos.nautical.losing_my_marbles.fabric.mixin.platform;

import org.spongepowered.asm.mixin.Mixin;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import one.devos.nautical.losing_my_marbles.content.marble.asset.MarbleAssetManager;

@Mixin(MarbleAssetManager.class)
public abstract class MarbleAssetManagerMixin implements IdentifiableResourceReloadListener {
	@Override
	public ResourceLocation getFabricId() {
		return MarbleAssetManager.ID;
	}
}
