package one.devos.nautical.losing_my_marbles.framework.phys.util;

import com.github.stephengold.joltjni.ShapeRefC;

import it.unimi.dsi.fastutil.floats.FloatUnaryOperator;
import net.minecraft.util.Mth;

public final class CurveGenerator {
	private final float centerX;
	private final float centerZ;

	private final float radius;

	private final float from;
	private final float to;

	private final int steps;

	public CurveGenerator(float centerX, float centerZ, float radius, float from, float to, int steps) {
		this.centerX = centerX;
		this.centerZ = centerZ;
		this.radius = radius;
		this.from = from;
		this.to = to;
		this.steps = steps;
	}

	public ShapeRefC create(FloatUnaryOperator scaler, float y, float height) {
		TriStripBuilder builder = new TriStripBuilder(scaler);
		this.forEachPoint((x, z) -> builder.then(x, y, z).then(x, y + height, z));
		return builder.build();
	}

	public void forEachPoint(PointConsumer consumer) {
		for (int i = 0; i <= this.steps; i++) {
			float progress = (1f / this.steps) * i;
			float theta = Mth.lerp(progress, this.from, this.to);

			float x = -Mth.cos(theta) * this.radius + this.centerX;
			float z = Mth.sin(theta) * this.radius + this.centerZ;

			consumer.accept(x, z);
		}
	}

	@FunctionalInterface
	public interface PointConsumer {
		void accept(float x, float z);
	}
}
