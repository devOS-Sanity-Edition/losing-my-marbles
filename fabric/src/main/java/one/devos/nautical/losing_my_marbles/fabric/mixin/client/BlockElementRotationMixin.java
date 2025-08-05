package one.devos.nautical.losing_my_marbles.fabric.mixin.client;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.client.renderer.block.model.BlockElementRotation;
import one.devos.nautical.losing_my_marbles.framework.extension.BlockElementRotationExt;

@Mixin(BlockElementRotation.class)
public class BlockElementRotationMixin implements BlockElementRotationExt {
	@Unique
	private Vector3f angles;

	@Override
	@Nullable
	public Vector3f lmm$angles() {
		return this.angles;
	}

	@Override
	public void lmm$setAngles(Vector3f angles) {
		this.angles = angles;
	}
}
