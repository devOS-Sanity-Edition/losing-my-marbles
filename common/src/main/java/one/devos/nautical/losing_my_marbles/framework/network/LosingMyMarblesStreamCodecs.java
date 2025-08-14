package one.devos.nautical.losing_my_marbles.framework.network;

import java.util.List;
import java.util.function.Function;

import com.mojang.serialization.DataResult;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public final class LosingMyMarblesStreamCodecs {
	public static final StreamCodec<ByteBuf, float[]> FLOAT_ARRAY = new StreamCodec<>() {
		@Override
		public float[] decode(ByteBuf buf) {
			int size = VarInt.read(buf);
			float[] array = new float[size];
			for (int i = 0; i < size; i++) {
				array[i] = buf.readFloat();
			}
			return array;
		}

		@Override
		public void encode(ByteBuf buf, float[] array) {
			VarInt.write(buf, array.length);
			for (float f : array) {
				buf.writeFloat(f);
			}
		}
	};

	public static final StreamCodec<ByteBuf, Float> NORMALIZED_FLOAT = floatRange(0, 1);

	public static final StreamCodec<ByteBuf, Float> POSITIVE_FLOAT = validate(ByteBufCodecs.FLOAT, value -> {
		if (value <= 0) {
			return DataResult.error(() -> "Value must be >0: " + value);
		} else {
			return DataResult.success(value);
		}
	});

	public static StreamCodec<ByteBuf, Float> floatRange(float minInclusive, float maxInclusive) {
		return validate(ByteBufCodecs.FLOAT, value -> {
					if (value < minInclusive || value > maxInclusive) {
						return DataResult.error(() -> "Value must be between " + minInclusive + " and " + maxInclusive + ": " + value);
					} else {
						return DataResult.success(value);
					}
				}
		);
	}

	public static <B extends ByteBuf, T> StreamCodec<B, List<T>> list(StreamCodec<B, T> codec, int minSize, int maxSize) {
		StreamCodec<B, List<T>> listCodec = codec.apply(ByteBufCodecs.list(maxSize));
		return validate(listCodec, list -> {
			if (list.size() < minSize) {
				return DataResult.error(() -> "List must contain at least " + minSize + " elements");
			} else {
				return DataResult.success(list);
			}
		});
	}

	public static <B, T> StreamCodec<B, T> validate(StreamCodec<B, T> codec, Function<T, DataResult<T>> validator) {
		return new StreamCodec<>() {
			@Override
			public T decode(B buffer) {
				DataResult<T> result = validator.apply(codec.decode(buffer));
				if (result instanceof DataResult.Error<T> error) {
					throw new DecoderException(error.message());
				} else {
					return result.getOrThrow();
				}
			}

			@Override
			public void encode(B buffer, T value) {
				DataResult<T> result = validator.apply(value);
				if (result instanceof DataResult.Error<T> error) {
					throw new EncoderException(error.message());
				} else {
					codec.encode(buffer, value);
				}
			}
		};
	}

	private LosingMyMarblesStreamCodecs() {
	}
}
