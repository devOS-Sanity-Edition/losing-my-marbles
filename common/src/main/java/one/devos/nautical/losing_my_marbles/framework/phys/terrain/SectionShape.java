package one.devos.nautical.losing_my_marbles.framework.phys.terrain;

import com.github.stephengold.joltjni.BodyCreationSettings;
import com.github.stephengold.joltjni.ShapeResult;
import com.github.stephengold.joltjni.StaticCompoundShapeSettings;
import com.github.stephengold.joltjni.readonly.ConstShape;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public record SectionShape(ConstShape shape, Properties properties) {
	public void configure(BodyCreationSettings settings) {
		settings.setShape(this.shape);
		this.properties.configure(settings);
	}

	public record Properties(float friction, float restitution) {
		public void configure(BodyCreationSettings settings) {
			settings.setFriction(this.friction);
			settings.setRestitution(this.restitution);
		}

		public static Properties of(BlockState state) {
			return new Properties(state.getBlock().getFriction(), 0.5f);
		}
	}

	public static final class Builder {
		private final Properties properties;
		private final BoxCache boxCache;
		private final StaticCompoundShapeSettings settings;

		public Builder(Properties properties, BoxCache boxCache) {
			this.properties = properties;
			this.boxCache = boxCache;
			this.settings = new StaticCompoundShapeSettings();
		}

		public void add(int x, int y, int z, AABB box) {
			Vec3 center = box.getCenter();

			this.settings.addShape(
					(float) (x + center.x), (float) (y + center.y), (float) (z + center.z),
					this.boxCache.get(box)
			);
		}

		public SectionShape build() {
			ShapeResult result = this.settings.create();
			if (result.hasError()) {
				throw new RuntimeException("Failed to build shape for section: " + result.getError());
			}

			return new SectionShape(result.get(), this.properties);
		}
	}
}
