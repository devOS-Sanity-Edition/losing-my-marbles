package one.devos.nautical.losing_my_marbles.framework.phys.debug;

import java.nio.FloatBuffer;

public interface DebugGeometryOutput {
	void accept(FloatBuffer triangleVertices);
}
