package one.devos.nautical.losing_my_marbles.fabric.mixin;

import net.minecraft.server.level.ServerLevel;

import one.devos.nautical.losing_my_marbles.framework.phys.PhysicsEnvironment;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {
	@Inject(method = "close", at = @At("TAIL"))
	private void onClose(CallbackInfo ci) {
		PhysicsEnvironment.get((ServerLevel) (Object) this).close();
	}
}
