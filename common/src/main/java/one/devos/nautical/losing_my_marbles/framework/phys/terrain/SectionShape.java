package one.devos.nautical.losing_my_marbles.framework.phys.terrain;

import com.github.stephengold.joltjni.BodyCreationSettings;
import com.github.stephengold.joltjni.ShapeRefC;
import com.github.stephengold.joltjni.ShapeResult;
import com.github.stephengold.joltjni.StaticCompoundShapeSettings;

import net.minecraft.core.Cursor3D;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public record SectionShape(ShapeRefC shape, Properties properties) {
	public void configure(BodyCreationSettings settings) {
		settings.setShape(this.shape);
		// setShape creates a new reference to the shape, close this one now that we're done with it
		this.shape.close();

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

		public void add(Cursor3D cursor, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
			this.add(cursor.nextX(), cursor.nextY(), cursor.nextZ(), minX, minY, minZ, maxX, maxY, maxZ);
		}

		public void add(int x, int y, int z, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
			float centerX = (float) Mth.lerp(0.5, minX, maxX);
			float centerY = (float) Mth.lerp(0.5, minY, maxY);
			float centerZ = (float) Mth.lerp(0.5, minZ, maxZ);

			this.settings.addShape(
					x + centerX, y + centerY, z + centerZ,
					this.boxCache.get(minX, minY, minZ, maxX, maxY, maxZ)
			);
		}

		public SectionShape build() {
			try (ShapeResult result = this.settings.create()) {
				// close the settings now that we're done with them.
				// it's ref-counted, so we need to create and close a reference.
				this.settings.toRef().close();

				if (result.hasError()) {
					throw new RuntimeException("Failed to build shape for section: " + result.getError());
				}

				return new SectionShape(result.get(), this.properties);
			}
		}
	}
}
