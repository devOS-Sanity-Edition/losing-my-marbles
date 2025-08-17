package one.devos.nautical.losing_my_marbles.content.marble.effect.contact.entity.impl;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.world.entity.Entity;
import one.devos.nautical.losing_my_marbles.content.marble.MarbleEntity;
import one.devos.nautical.losing_my_marbles.content.marble.effect.contact.entity.EntityContactEffect;

public record ApplyComponents(DataComponentMap components) implements EntityContactEffect {
	public static final MapCodec<ApplyComponents> CODEC = DataComponentMap.CODEC.fieldOf("components").xmap(ApplyComponents::new, ApplyComponents::components);

	@Override
	public void apply(MarbleEntity entity, Entity target) {
		for (TypedDataComponent<?> typed : this.components) {
			apply(typed, target);
		}
	}

	@Override
	public MapCodec<? extends EntityContactEffect> codec() {
		return CODEC;
	}

	private static <T> void apply(TypedDataComponent<T> typed, Entity entity) {
		entity.setComponent(typed.type(), typed.value());
	}
}
