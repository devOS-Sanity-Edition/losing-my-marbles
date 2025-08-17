package one.devos.nautical.losing_my_marbles.content;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import one.devos.nautical.losing_my_marbles.LosingMyMarbles;
import one.devos.nautical.losing_my_marbles.content.marble.data.MarbleType;
import one.devos.nautical.losing_my_marbles.content.marble.data.shape.MarbleShape;
import one.devos.nautical.losing_my_marbles.content.marble.effect.bounce.BounceEffect;
import one.devos.nautical.losing_my_marbles.content.marble.effect.contact.block.BlockContactEffect;
import one.devos.nautical.losing_my_marbles.content.marble.effect.contact.entity.EntityContactEffect;
import one.devos.nautical.losing_my_marbles.framework.platform.PlatformHelper;

public final class LosingMyMarblesRegistries {
	// static
	public static final Registry<MarbleShape.Type<?>> MARBLE_SHAPE_TYPE = create("marble_shape_type");
	public static final Registry<MapCodec<? extends BounceEffect>> BOUNCE_EFFECT_TYPE = create("bounce_effect_type");
	public static final Registry<MapCodec<? extends BlockContactEffect>> BLOCK_CONTACT_EFFECT_TYPE = create("contact_effect_type/block");
	public static final Registry<MapCodec<? extends EntityContactEffect>> ENTITY_CONTACT_EFFECT_TYPE = create("contact_effect_type/entity");
	// dynamic
	public static final ResourceKey<Registry<MarbleType>> MARBLE_TYPE = key("marble_type");

	public static void init() {
		PlatformHelper.INSTANCE.registerDynamicRegistry(MARBLE_TYPE, MarbleType.DIRECT_CODEC);
	}

	private static <T> ResourceKey<Registry<T>> key(String name) {
		return ResourceKey.createRegistryKey(LosingMyMarbles.id(name));
	}

	private static <T> Registry<T> create(String name) {
		ResourceKey<Registry<T>> key = key(name);
		return PlatformHelper.INSTANCE.registerStaticRegistry(key, null);
	}
}
