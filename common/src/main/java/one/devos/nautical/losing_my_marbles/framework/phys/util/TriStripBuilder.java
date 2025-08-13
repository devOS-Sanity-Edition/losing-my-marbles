package one.devos.nautical.losing_my_marbles.framework.phys.util;

import com.github.stephengold.joltjni.Float3;
import com.github.stephengold.joltjni.IndexedTriangle;
import com.github.stephengold.joltjni.IndexedTriangleList;
import com.github.stephengold.joltjni.MeshShapeSettings;
import com.github.stephengold.joltjni.ShapeRefC;
import com.github.stephengold.joltjni.ShapeResult;
import com.github.stephengold.joltjni.VertexList;

public final class TriStripBuilder {
	private final VertexList vertices;
	private final IndexedTriangleList triangles;

	private boolean flip;

	public TriStripBuilder() {
		this.vertices = new VertexList();
		this.triangles = new IndexedTriangleList();
	}

	public TriStripBuilder then(float x, float y, float z) {
		this.vertices.pushBack(new Float3(x, y, z));

		if (this.vertices.size() >= 3) {
			int v3 = this.vertices.size() - 1;
			int v2 = v3 - 1;
			int v1 = v2 - 1;

			if (this.flip) {
				int temp = v1;
				v1 = v2;
				v2 = temp;
			}

			this.triangles.pushBack(new IndexedTriangle(v1, v2, v3));
			this.flip = !this.flip;
		}

		return this;
	}

	public ShapeRefC build() {
		if (this.vertices.size() < 3) {
			throw new IllegalStateException("Too few vertices: " + this.vertices);
		}

		MeshShapeSettings settings = new MeshShapeSettings(this.vertices, this.triangles);

		try (ShapeResult result = settings.create()) {
			settings.toRef().close();

			if (result.hasError()) {
				throw new RuntimeException("Failed to create MeshShape: " + result.getError());
			}

			return result.get();
		}
	}
}
