package one.devos.nautical.losing_my_marbles.content.marble.effect.contact.block.impl;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import one.devos.nautical.losing_my_marbles.content.marble.MarbleEntity;
import one.devos.nautical.losing_my_marbles.content.marble.effect.contact.block.BlockContactEffect;

public record Swap(BlockPredicate predicate, Block replacement) implements BlockContactEffect {
	public static final MapCodec<Swap> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			BlockPredicate.CODEC.fieldOf("predicate").forGetter(Swap::predicate),
			BuiltInRegistries.BLOCK.byNameCodec().fieldOf("replacement").forGetter(Swap::replacement)
	).apply(i, Swap::new));

	@Override
	public void apply(MarbleEntity entity, BlockState state, BlockPos pos) {
		if (!(entity.level() instanceof ServerLevel level) || !this.predicate.test(level, pos))
			return;

		BlockState newState = this.replacement.withPropertiesOf(state);
		level.setBlockAndUpdate(pos, newState);
	}

	@Override
	public MapCodec<? extends BlockContactEffect> codec() {
		return CODEC;
	}
}
