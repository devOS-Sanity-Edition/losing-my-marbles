package one.devos.nautical.losing_my_marbles.fabric.mixin.client;

import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.client.renderer.block.model.BlockElementRotation;
import net.minecraft.core.Direction;
import one.devos.nautical.losing_my_marbles.framework.extension.BlockElementRotationExt;

@Mixin(targets = "net.minecraft.client.renderer.block.model.BlockElement$Deserializer")
public abstract class BlockElement_DeserializerMixin {
	@Shadow
	protected abstract Vector3f getVector3f(JsonObject jsonObject, String string);

	@WrapOperation(
			method = "getRotation",
			at = @At(
					value = "NEW",
					target = "(Lorg/joml/Vector3f;Lnet/minecraft/core/Direction$Axis;FZ)Lnet/minecraft/client/renderer/block/model/BlockElementRotation;"
			)
	)
	private BlockElementRotation parseRotatedField(Vector3f vector3f, Direction.Axis axis, float f, boolean bl, Operation<BlockElementRotation> original, @Local(argsOnly = true) JsonObject obj) {
		BlockElementRotation blockElementRotation = original.call(vector3f, axis, f, bl);
		if (obj.has("rotated")) {
			((BlockElementRotationExt) (Object) blockElementRotation).lmm$setAngles(this.getVector3f(obj, "rotated"));
		}
		return blockElementRotation;
	}
}
