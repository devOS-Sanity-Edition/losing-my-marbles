package one.devos.nautical.losing_my_marbles.content;

import com.mojang.serialization.Codec;

import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.block.Block;
import one.devos.nautical.losing_my_marbles.LosingMyMarbles;
import one.devos.nautical.losing_my_marbles.content.marble.MarbleEntity;
import one.devos.nautical.losing_my_marbles.content.marble.StoredMarble;
import one.devos.nautical.losing_my_marbles.content.marble.asset.MarbleAsset;
import one.devos.nautical.losing_my_marbles.content.marble.data.shape.MarbleShape;
import one.devos.nautical.losing_my_marbles.content.marble.effect.bounce.BounceEffect;
import one.devos.nautical.losing_my_marbles.content.marble.effect.contact.block.BlockContactEffect;
import one.devos.nautical.losing_my_marbles.content.marble.effect.contact.entity.EntityContactEffect;
import one.devos.nautical.losing_my_marbles.framework.data.LosingMyMarblesCodecs;
import one.devos.nautical.losing_my_marbles.framework.network.LosingMyMarblesStreamCodecs;

public final class LosingMyMarblesDataComponents {
	// for items
	public static final DataComponentType<StoredMarble> MARBLE = register(
			"marble", StoredMarble.CODEC, StoredMarble.STREAM_CODEC
	);
	// for marbles
	public static final DataComponentType<Float> FRICTION = register(
			"friction", LosingMyMarblesCodecs.NORMALIZED_FLOAT, LosingMyMarblesStreamCodecs.NORMALIZED_FLOAT
	);
	public static final DataComponentType<Float> RESTITUTION = register(
			"restitution", LosingMyMarblesCodecs.NORMALIZED_FLOAT, LosingMyMarblesStreamCodecs.NORMALIZED_FLOAT
	);
	public static final DataComponentType<Float> MASS = register(
			"mass", Codec.floatRange(0, Float.MAX_VALUE), LosingMyMarblesStreamCodecs.floatRange(0, Float.MAX_VALUE)
	);
	public static final DataComponentType<Float> SCALE = register(
			"scale", Codec.floatRange(MarbleEntity.MIN_SCALE, MarbleEntity.MAX_SCALE), LosingMyMarblesStreamCodecs.floatRange(MarbleEntity.MIN_SCALE, MarbleEntity.MAX_SCALE)
	);
	public static final DataComponentType<Float> GRAVITY_SCALE = register(
			"gravity_scale", Codec.floatRange(-8, 8), LosingMyMarblesStreamCodecs.floatRange(-8, 8)
	);
	public static final DataComponentType<MarbleShape> SHAPE = register(
			"shape", MarbleShape.CODEC, MarbleShape.STREAM_CODEC
	);
	public static final DataComponentType<ResourceKey<MarbleAsset>> ASSET = register(
			"asset", ResourceKey.codec(MarbleAsset.REGISTRY_KEY), ResourceKey.streamCodec(MarbleAsset.REGISTRY_KEY)
	);
	public static final DataComponentType<HolderSet<DamageType>> DAMAGE_IMMUNE = register(
			"damage_immune", RegistryCodecs.homogeneousList(Registries.DAMAGE_TYPE), LosingMyMarblesStreamCodecs.homogeneousList(Registries.DAMAGE_TYPE)
	);
	public static final DataComponentType<HolderSet<Block>> ACCUMULATES_MASS = register(
			"accumulates_mass", RegistryCodecs.homogeneousList(Registries.BLOCK), LosingMyMarblesStreamCodecs.homogeneousList(Registries.BLOCK)
	);
	public static final DataComponentType<Unit> NO_PICKUP = register(
			"no_pickup", Unit.CODEC, Unit.STREAM_CODEC
	);
	public static final DataComponentType<BounceEffect> BOUNCE_EFFECT = register(
			"bounce_effect", BounceEffect.CODEC, BounceEffect.STREAM_CODEC
	);
	public static final DataComponentType<BlockContactEffect> BLOCK_CONTACT_EFFECT = register(
			"contact_effect/block", BlockContactEffect.CODEC, BlockContactEffect.STREAM_CODEC
	);
	public static final DataComponentType<EntityContactEffect> ENTITY_CONTACT_EFFECT = register(
			"contact_effect/entity", EntityContactEffect.CODEC, EntityContactEffect.STREAM_CODEC
	);

	public static void init() {
	}

	private static <T> DataComponentType<T> register(String name, Codec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
		ResourceLocation id = LosingMyMarbles.id(name);
		DataComponentType<T> type = DataComponentType.<T>builder().persistent(codec).networkSynchronized(streamCodec).build();
		return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, id, type);
	}
}
