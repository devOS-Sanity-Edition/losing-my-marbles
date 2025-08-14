package one.devos.nautical.losing_my_marbles.content.marble.data.shape;

import com.github.stephengold.joltjni.ShapeRefC;
import com.github.stephengold.joltjni.readonly.ConstShape;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesRegistries;

public interface MarbleShape {
	Codec<MarbleShape> CODEC = LosingMyMarblesRegistries.MARBLE_SHAPE_TYPE.byNameCodec()
			.dispatch(MarbleShape::type, Type::codec);
	StreamCodec<RegistryFriendlyByteBuf, MarbleShape> STREAM_CODEC = ByteBufCodecs.registry(LosingMyMarblesRegistries.MARBLE_SHAPE_TYPE.key())
			.dispatch(MarbleShape::type, Type::streamCodec);

	CreatedShape createJoltShape();

	Type<?> type();

	record Type<T extends MarbleShape>(MapCodec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
	}

	/**
	 * @param afterAssign runnable invoked after the shape has been assigned to a body
	 */
	record CreatedShape(ConstShape shape, Runnable afterAssign) {
		public CreatedShape(ConstShape shape) {
			this(shape, () -> {});
		}

		public CreatedShape(ShapeRefC ref) {
			this(ref, ref::close);
		}
	}
}
