package one.devos.nautical.losing_my_marbles.content.marble.effect.contact.block.impl;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import one.devos.nautical.losing_my_marbles.content.marble.MarbleEntity;
import one.devos.nautical.losing_my_marbles.content.marble.effect.contact.block.BlockContactEffect;

public record Compound(List<BlockContactEffect> effects) implements BlockContactEffect {
	public static final Codec<Compound> CODEC = BlockContactEffect.CODEC.listOf(2, Integer.MAX_VALUE).xmap(Compound::new, Compound::effects);
	public static final MapCodec<Compound> MAP_CODEC = CODEC.fieldOf("effects");

	@Override
	public void apply(MarbleEntity entity, BlockState state, BlockPos pos) {
		for (BlockContactEffect effect : this.effects) {
			effect.apply(entity, state, pos);
		}
	}

	@Override
	public MapCodec<? extends BlockContactEffect> codec() {
		return MAP_CODEC;
	}
}
