package one.devos.nautical.losing_my_marbles.fabric.mixin.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import com.mojang.serialization.MapCodec;

import net.minecraft.stats.RecipeBookSettings.TypeSettings;

@Mixin(TypeSettings.class)
public interface TypeSettingsAccessor {
	@Invoker("codec")
	static MapCodec<TypeSettings> lmm$codec(String open, String filtering) {
		throw new AssertionError();
	}
}
