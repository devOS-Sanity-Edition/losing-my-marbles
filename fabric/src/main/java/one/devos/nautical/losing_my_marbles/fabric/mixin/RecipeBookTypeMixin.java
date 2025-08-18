package one.devos.nautical.losing_my_marbles.fabric.mixin;

import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.inventory.RecipeBookType;

@Mixin(RecipeBookType.class)
public class RecipeBookTypeMixin {
	@Shadow
	@Final
	@Mutable
	private static RecipeBookType[] $VALUES;

	@SuppressWarnings("SameParameterValue")
	@Invoker("<init>")
	private static RecipeBookType create(String name, int ordinal) {
		throw new AssertionError();
	}

	static {
		var entry = create("MARBLE_MAKER", $VALUES.length);

		$VALUES = ArrayUtils.addAll($VALUES, entry);
	}
}
