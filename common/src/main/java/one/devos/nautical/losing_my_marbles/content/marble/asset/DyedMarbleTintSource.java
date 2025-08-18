package one.devos.nautical.losing_my_marbles.content.marble.asset;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.ARGB;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesDataComponents;
import one.devos.nautical.losing_my_marbles.content.marble.StoredMarble;

public record DyedMarbleTintSource(int defaultColor) implements ItemTintSource {
	public static final MapCodec<DyedMarbleTintSource> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			ExtraCodecs.RGB_COLOR_CODEC.fieldOf("default").forGetter(DyedMarbleTintSource::defaultColor)
	).apply(i, DyedMarbleTintSource::new));

	@Override
	public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
		StoredMarble marble = stack.get(LosingMyMarblesDataComponents.MARBLE);
		if (marble == null) {
			return this.defaultColor;
		}

		HolderLookup.Provider registries = getRegistries(level, entity);
		if (registries == null) {
			return this.defaultColor;
		}

		return marble.get(registries)
				.flatMap(instance -> instance.getOptional(DataComponents.DYED_COLOR))
				.map(color -> ARGB.opaque(color.rgb()))
				.orElse(this.defaultColor);
	}

	@Override
	public MapCodec<? extends ItemTintSource> type() {
		return MAP_CODEC;
	}

	@Nullable
	private static HolderLookup.Provider getRegistries(@Nullable ClientLevel level, @Nullable LivingEntity entity) {
		if (level != null) {
			return level.registryAccess();
		} else if (entity != null) {
			return entity.registryAccess();
		} else {
			return null;
		}
	}
}
