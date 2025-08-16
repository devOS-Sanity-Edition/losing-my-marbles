package one.devos.nautical.losing_my_marbles.content.marble.effect.bounce.impl;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.phys.Vec3;
import one.devos.nautical.losing_my_marbles.content.marble.MarbleEntity;
import one.devos.nautical.losing_my_marbles.content.marble.effect.bounce.BounceEffect;

public record PlaceBlock(BlockStateProvider state) implements BounceEffect {
	public static final MapCodec<PlaceBlock> CODEC = BlockStateProvider.CODEC.fieldOf("state").xmap(PlaceBlock::new, PlaceBlock::state);

	@Override
	public void apply(MarbleEntity entity, Vec3 oldVel, Vec3 newVel) {
		Level level = entity.level();
		if (level.isClientSide())
			return;

		BlockPos pos = entity.blockPosition();
		if (!level.getBlockState(pos).canBeReplaced())
			return;

		BlockState state = this.state.getState(entity.getRandom(), pos);
		level.setBlockAndUpdate(pos, state);
	}

	@Override
	public MapCodec<? extends BounceEffect> codec() {
		return CODEC;
	}
}
