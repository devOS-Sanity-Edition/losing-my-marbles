package one.devos.nautical.losing_my_marbles.framework.phys.terrain;

import com.github.stephengold.joltjni.BoxShapeSettings;

import com.github.stephengold.joltjni.ShapeResult;
import com.github.stephengold.joltjni.Vec3;
import com.github.stephengold.joltjni.readonly.ConstShape;

import it.unimi.dsi.fastutil.doubles.Double2ObjectMap;
import it.unimi.dsi.fastutil.doubles.Double2ObjectMaps;
import it.unimi.dsi.fastutil.doubles.Double2ObjectOpenHashMap;

public final class BoxCache {
	// I think someone needs to kill me for this. it saves a lot of AABB allocations though.
	private final Double2ObjectMap<Double2ObjectMap<Double2ObjectMap<Double2ObjectMap<Double2ObjectMap<Double2ObjectMap<ConstShape>>>>>> cache = newMap();

	public ConstShape get(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		Double2ObjectMap<ConstShape> innermostMap = this.cache.computeIfAbsent(minX, BoxCache::newMap)
				.computeIfAbsent(minY, BoxCache::newMap)
				.computeIfAbsent(minZ, BoxCache::newMap)
				.computeIfAbsent(maxX, BoxCache::newMap)
				.computeIfAbsent(maxY, BoxCache::newMap);

		ConstShape shape = innermostMap.get(maxZ);
		if (shape != null)
			return shape;

		ConstShape created = create(minX, minY, minZ, maxX, maxY, maxZ);
		innermostMap.put(maxZ, created);
		return created;
	}

	public void close() {
		this.cache.values().stream()
				.flatMap(map -> map.values().stream())
				.flatMap(map -> map.values().stream())
				.flatMap(map -> map.values().stream())
				.flatMap(map -> map.values().stream())
				.flatMap(map -> map.values().stream())
				.forEach(ConstShape::close);
	}

	private static ConstShape create(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		double extent = (maxX - minX) / 2;
		double eytent = (maxY - minY) / 2;
		double eztent = (maxZ - minZ) / 2;

		try (BoxShapeSettings settings = new BoxShapeSettings()) {
			settings.setHalfExtent(new Vec3(extent, eytent, eztent));
			settings.setConvexRadius(0);

			ShapeResult result = settings.create();
			if (result.hasError()) {
				throw new RuntimeException("Failed to create BoxShape: " + result.getError());
			}

			return result.get();
		}
	}

	private static <T> Double2ObjectMap<T> newMap() {
		return Double2ObjectMaps.synchronize(new Double2ObjectOpenHashMap<>());
	}

	private static <T> Double2ObjectMap<T> newMap(double ignored) {
		return newMap();
	}
}
