package one.devos.nautical.losing_my_marbles.fabric.mixin;

import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import net.minecraft.world.level.block.state.BlockState;

import one.devos.nautical.losing_my_marbles.framework.phys.PhysicsEventListeners;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public class LevelMixin {
	@Inject(
			method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/Level;setBlocksDirty(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;)V"
			)
	)
	private void onStateChange(CallbackInfoReturnable<Boolean> cir,
							   @Local(argsOnly = true) BlockPos pos,
							   @Local(argsOnly = true) BlockState newState,
							   @Local(ordinal = 1) BlockState oldState) {
		if ((Object) this instanceof ServerLevel serverLevel) {
			PhysicsEventListeners.blockChanged(serverLevel, pos, oldState, newState);
		}
	}
}
