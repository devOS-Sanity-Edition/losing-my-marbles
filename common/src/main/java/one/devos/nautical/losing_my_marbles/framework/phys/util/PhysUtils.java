package one.devos.nautical.losing_my_marbles.framework.phys.util;

import com.github.stephengold.joltjni.ShapeRefC;
import com.github.stephengold.joltjni.ShapeResult;
import com.github.stephengold.joltjni.StaticCompoundShapeSettings;
import com.github.stephengold.joltjni.TriangleShape;
import com.github.stephengold.joltjni.readonly.Vec3Arg;

public final class PhysUtils {
	public static ShapeRefC quad(Vec3Arg bottomLeft, Vec3Arg bottomRight, Vec3Arg topRight, Vec3Arg topLeft) {
		StaticCompoundShapeSettings settings = new StaticCompoundShapeSettings();

		settings.addShape(0, 0, 0, new TriangleShape(bottomLeft, bottomRight, topRight));
		settings.addShape(0, 0, 0, new TriangleShape(topRight, topLeft, bottomLeft));

		try (ShapeResult result = settings.create()) {
			settings.toRef().close();

			if (result.hasError()) {
				throw new RuntimeException("Failed to create quad: " + result.getError());
			}

			return result.get();
		}
	}
}
