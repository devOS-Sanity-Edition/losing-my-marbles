package one.devos.nautical.losing_my_marbles.framework.phys.terrain;

import com.github.stephengold.joltjni.BoxShapeSettings;

import com.github.stephengold.joltjni.ShapeResult;
import com.github.stephengold.joltjni.Vec3;
import com.github.stephengold.joltjni.readonly.ConstShape;

import net.minecraft.world.phys.AABB;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class BoxCache {
	private final Map<AABB, ConstShape> cache = new ConcurrentHashMap<>();

	public ConstShape get(AABB box) {
		return this.cache.computeIfAbsent(box, BoxCache::create);
	}

	private static ConstShape create(AABB box) {
		try (BoxShapeSettings settings = new BoxShapeSettings()) {
			settings.setHalfExtent(new Vec3(box.getXsize() / 2, box.getYsize() / 2, box.getZsize() / 2));
			settings.setConvexRadius(0);

			ShapeResult result = settings.create();
			if (result.hasError()) {
				throw new RuntimeException("Failed to create BoxShape for AABB " + box + ": " + result.getError());
			}

			return result.get();
		}
	}
}
