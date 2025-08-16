package one.devos.nautical.losing_my_marbles.framework.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

public final class LosingMyMarblesCodecs {
	public static final Codec<Float> NORMALIZED_FLOAT = Codec.floatRange(0, 1);

	public static final Codec<Float> NON_ZERO_NORMALIZED_FLOAT = NORMALIZED_FLOAT.validate(
			value -> value == 0 ? DataResult.error(() -> "Value must not be 0") : DataResult.success(value)
	);

	public static final Codec<Float> OPEN_NORMALIZED_FLOAT = NON_ZERO_NORMALIZED_FLOAT.validate(
			value -> value == 1 ? DataResult.error(() -> "Value must not be 1") : DataResult.success(value)
	);

	public static final Codec<Float> POSITIVE_FLOAT = Codec.FLOAT.validate(value -> {
		if (value <= 0) {
			return DataResult.error(() -> "Value must be >0: " + value);
		} else {
			return DataResult.success(value);
		}
	});

	private LosingMyMarblesCodecs() {
	}
}
