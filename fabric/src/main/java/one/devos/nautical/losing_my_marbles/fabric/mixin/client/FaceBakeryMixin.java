package one.devos.nautical.losing_my_marbles.fabric.mixin.client;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import net.minecraft.client.renderer.block.model.BlockElementRotation;
import net.minecraft.client.renderer.block.model.FaceBakery;
import one.devos.nautical.losing_my_marbles.framework.extension.BlockElementRotationExt;

@Mixin(FaceBakery.class)
public class FaceBakeryMixin {
	@SuppressWarnings("NonConstantFieldWithUpperCaseName")
	@Shadow
	@Final
	private static Vector3fc NO_RESCALE;

	@Shadow
	private static void rotateVertexBy(Vector3f vector3f, Vector3fc vector3fc, Matrix4fc matrix4fc, Vector3fc vector3fc2) {
	}

	@WrapMethod(method = "applyElementRotation")
	private static void applyCoolerRotation(Vector3f vector3f, @Nullable BlockElementRotation blockElementRotation, Operation<Void> original) {
		if (blockElementRotation != null) {
			Vector3f angles = ((BlockElementRotationExt) (Object) blockElementRotation).lmm$angles();
			if (angles != null) {
				Matrix4f rotation = new Matrix4f().rotateXYZ(angles.mul((float) (Math.PI / 180F), new Vector3f()));
				rotateVertexBy(vector3f, blockElementRotation.origin(), rotation, NO_RESCALE);
			} else {
				original.call(vector3f, blockElementRotation);
			}
		}
	}
}
