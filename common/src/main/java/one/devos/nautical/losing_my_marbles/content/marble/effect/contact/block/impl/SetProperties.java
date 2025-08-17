package one.devos.nautical.losing_my_marbles.content.marble.effect.contact.block.impl;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import one.devos.nautical.losing_my_marbles.content.marble.MarbleEntity;
import one.devos.nautical.losing_my_marbles.content.marble.effect.contact.block.BlockContactEffect;

public record SetProperties(BlockPredicate predicate, BlockItemStateProperties properties) implements BlockContactEffect {
	public static final MapCodec<SetProperties> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			BlockPredicate.CODEC.fieldOf("predicate").forGetter(SetProperties::predicate),
			BlockItemStateProperties.CODEC.fieldOf("properties").forGetter(SetProperties::properties)
	).apply(i, SetProperties::new));

	@Override
	public void apply(MarbleEntity entity, BlockState state, BlockPos pos) {
		if (!(entity.level() instanceof ServerLevel level) || !this.predicate.test(level, pos))
			return;

		BlockState newState = this.properties.apply(state);
		level.setBlockAndUpdate(pos, newState);
	}

	@Override
	public MapCodec<? extends BlockContactEffect> codec() {
		return CODEC;
	}
}
