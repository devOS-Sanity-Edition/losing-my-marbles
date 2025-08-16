package one.devos.nautical.losing_my_marbles.content.marble.data.shape;

import com.github.stephengold.joltjni.ShapeRefC;
import com.github.stephengold.joltjni.Vec3;
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

	/**
	 * Default density value shapes should set. Based on a sphere with radius 0.1875 weighing 10kg.
	 */
	float DENSITY = 362.2f;

	/**
	 * @param scale an additional scaling factor to apply to the shape, >0
	 */
	CreatedShape createJoltShape(float scale);

	Type<?> type();

	record Type<T extends MarbleShape>(MapCodec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
	}

	/**
	 * @param offset an offset from an entity's position to the shape's position
	 * @param afterAssign runnable invoked after the shape has been assigned to a body
	 */
	record CreatedShape(ConstShape shape, Vec3 offset, Runnable afterAssign) implements AutoCloseable {
		public CreatedShape(ConstShape shape, Vec3 offset) {
			this(shape, offset, () -> {});
		}

		public CreatedShape(ShapeRefC ref, Vec3 offset) {
			this(ref, offset, ref::close);
		}

		@Override
		public void close() {
			this.afterAssign.run();
		}
	}
}
