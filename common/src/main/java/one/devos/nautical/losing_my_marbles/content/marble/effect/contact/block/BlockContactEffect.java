package one.devos.nautical.losing_my_marbles.content.marble.effect.contact.block;

import java.util.function.Function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.state.BlockState;
import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesRegistries;
import one.devos.nautical.losing_my_marbles.content.marble.MarbleEntity;
import one.devos.nautical.losing_my_marbles.content.marble.effect.contact.block.impl.Compound;

/**
 * Effect applied by a marble to all blocks it is touching every tick.
 */
public interface BlockContactEffect {
	Codec<BlockContactEffect> CODEC = Codec.withAlternative(
			LosingMyMarblesRegistries.BLOCK_CONTACT_EFFECT_TYPE.byNameCodec().dispatch(BlockContactEffect::codec, Function.identity()),
			Codec.lazyInitialized(() -> Compound.CODEC)
	);
	StreamCodec<RegistryFriendlyByteBuf, BlockContactEffect> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

	void apply(MarbleEntity entity, BlockState state, BlockPos pos);

	MapCodec<? extends BlockContactEffect> codec();
}
