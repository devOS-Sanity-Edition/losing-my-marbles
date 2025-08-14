package one.devos.nautical.losing_my_marbles.framework.phys.debug;

import java.nio.FloatBuffer;

import com.github.stephengold.joltjni.Body;
import com.github.stephengold.joltjni.Jolt;
import com.github.stephengold.joltjni.TransformedShape;

public interface DebugGeometryOutput {
	void accept(FloatBuffer triangleVertices);

	default void accept(Body body) {
		try (TransformedShape shape = body.getTransformedShape()) {
			int triangles = shape.countDebugTriangles();
			FloatBuffer buffer = Jolt.newDirectFloatBuffer(triangles * 3 * 3);
			shape.copyDebugTriangles(buffer);
			this.accept(buffer);
		}
	}
}
