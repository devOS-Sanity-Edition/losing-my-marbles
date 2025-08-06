package one.devos.nautical.losing_my_marbles.content;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import one.devos.nautical.losing_my_marbles.LosingMyMarbles;
import one.devos.nautical.losing_my_marbles.content.marble.MarbleEntity;
import one.devos.nautical.losing_my_marbles.content.marble.MarbleEntityRenderer;
import one.devos.nautical.losing_my_marbles.framework.platform.Env;
import one.devos.nautical.losing_my_marbles.framework.platform.PlatformClientHelper;
import one.devos.nautical.losing_my_marbles.framework.platform.PlatformHelper;

public final class LosingMyMarblesEntities {
	public static final EntityType<MarbleEntity> MARBLE = register(
			"marble",
			EntityType.Builder.of(MarbleEntity::new, MobCategory.MISC)
					.sized(MarbleEntity.DIAMETER, MarbleEntity.DIAMETER)
	);

	private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
		ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, LosingMyMarbles.id(name));
		EntityType<T> type = builder.build(key);
		return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, type);
	}

	public static void init() {
		if (PlatformHelper.INSTANCE.getEnvironment() == Env.CLIENT) {
			PlatformClientHelper.INSTANCE.registerEntityRenderer(MARBLE, MarbleEntityRenderer::new);
		}
	}
}
