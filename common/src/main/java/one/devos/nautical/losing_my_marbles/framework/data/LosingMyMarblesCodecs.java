package one.devos.nautical.losing_my_marbles.framework.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

public final class LosingMyMarblesCodecs {
	public static final Codec<Float> NORMALIZED_FLOAT = Codec.floatRange(0, 1);

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
