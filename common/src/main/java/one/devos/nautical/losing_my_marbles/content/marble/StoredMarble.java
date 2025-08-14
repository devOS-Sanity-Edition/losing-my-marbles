package one.devos.nautical.losing_my_marbles.content.marble;

import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.EitherHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesRegistries;
import one.devos.nautical.losing_my_marbles.content.marble.data.MarbleInstance;
import one.devos.nautical.losing_my_marbles.content.marble.data.MarbleType;
import one.devos.nautical.losing_my_marbles.framework.network.LosingMyMarblesStreamCodecs;

public sealed interface StoredMarble extends TooltipProvider {
	Codec<StoredMarble> CODEC = StorageType.CODEC.dispatch(StoredMarble::type, type -> type.codec);
	StreamCodec<RegistryFriendlyByteBuf, StoredMarble> STREAM_CODEC = StorageType.STREAM_CODEC.dispatch(StoredMarble::type, type -> type.streamCodec);

	Component TOOLTIP_HEADER = Component.translatable("item.losing_my_marbles.stored_marble.tooltip").withStyle(ChatFormatting.GRAY);
	String INVALID_KEY = "item.losing_my_marbles.stored_marble.invalid";

	Optional<MarbleInstance> get(HolderLookup.Provider registries);

	Component nameForTooltip(HolderLookup.Provider registries);

	StorageType type();

	@Override
	default void addToTooltip(Item.TooltipContext context, Consumer<Component> output, TooltipFlag flag, DataComponentGetter components) {
		output.accept(TOOLTIP_HEADER);
		output.accept(CommonComponents.space().append(this.nameForTooltip(context.registries())));
	}

	record Type(EitherHolder<MarbleType> holder) implements StoredMarble {
		public static final MapCodec<Type> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
				EitherHolder.codec(LosingMyMarblesRegistries.MARBLE_TYPE, MarbleType.CODEC).fieldOf("holder").forGetter(Type::holder)
		).apply(i, Type::new));

		public static final StreamCodec<RegistryFriendlyByteBuf, Type> STREAM_CODEC = StreamCodec.composite(
				EitherHolder.streamCodec(LosingMyMarblesRegistries.MARBLE_TYPE, MarbleType.STREAM_CODEC), Type::holder,
				Type::new
		);

		public Type(ResourceKey<MarbleType> key) {
			this(new EitherHolder<>(key));
		}

		@Override
		public Optional<MarbleInstance> get(HolderLookup.Provider registries) {
			return this.holder.unwrap(registries).map(MarbleInstance::new);
		}

		@Override
		public Component nameForTooltip(HolderLookup.Provider registries) {
			return this.holder.contents().map(
					holder -> holder.value().name(),
					key -> registries.get(key)
							.map(holder -> holder.value().name())
							.orElseGet(() -> Component.translatable(INVALID_KEY).withStyle(ChatFormatting.RED))
			);
		}

		@Override
		public StorageType type() {
			return StorageType.TYPE;
		}
	}

	record Instance(MarbleInstance instance) implements StoredMarble {
		public static final MapCodec<Instance> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
				MarbleInstance.CODEC.fieldOf("instance").forGetter(Instance::instance)
		).apply(i, Instance::new));

		public static final StreamCodec<RegistryFriendlyByteBuf, Instance> STREAM_CODEC = StreamCodec.composite(
				MarbleInstance.STREAM_CODEC, Instance::instance,
				Instance::new
		);

		@Override
		public Optional<MarbleInstance> get(HolderLookup.Provider registries) {
			return Optional.of(this.instance.copy());
		}

		@Override
		public Component nameForTooltip(HolderLookup.Provider registries) {
			return this.instance.type().name();
		}

		@Override
		public StorageType type() {
			return StorageType.INSTANCE;
		}
	}

	enum StorageType implements StringRepresentable {
		TYPE(Type.CODEC, Type.STREAM_CODEC),
		INSTANCE(Instance.CODEC, Instance.STREAM_CODEC);

		public static final Codec<StorageType> CODEC = StringRepresentable.fromEnum(StorageType::values);
		public static final StreamCodec<RegistryFriendlyByteBuf, StorageType> STREAM_CODEC = LosingMyMarblesStreamCodecs.ofEnum(StorageType.class);

		public final MapCodec<? extends StoredMarble> codec;
		public final StreamCodec<RegistryFriendlyByteBuf, ? extends StoredMarble> streamCodec;

		private final String name;

		StorageType(MapCodec<? extends StoredMarble> codec, StreamCodec<RegistryFriendlyByteBuf, ? extends StoredMarble> streamCodec) {
			this.codec = codec;
			this.streamCodec = streamCodec;
			this.name = this.name().toLowerCase(Locale.ROOT);
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}
	}
}
