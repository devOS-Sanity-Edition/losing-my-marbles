package one.devos.nautical.losing_my_marbles.framework.phys.terrain;

import com.github.stephengold.joltjni.BoxShape;
import com.github.stephengold.joltjni.BoxShapeSettings;
import com.github.stephengold.joltjni.CompoundShapeSettings;
import com.github.stephengold.joltjni.StaticCompoundShapeSettings;
import com.github.stephengold.joltjni.readonly.ConstShapeSettings;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.Nullable;

public final class BlockShapeFactory {
	@Nullable
	public static ConstShapeSettings create(Level level, BlockState state, BlockPos pos) {
		VoxelShape shape = state.getCollisionShape(level, pos);
		if (shape.isEmpty()) {
			return null;
		}

		{
			BoxShapeSettings settings = new BoxShapeSettings();
			settings.setHalfExtent(new com.github.stephengold.joltjni.Vec3(1 / 2f, 3 / 16f / 2f, 1 / 2f));
			if (false) return settings;
		}

		CompoundShapeSettings settings = new StaticCompoundShapeSettings();

		for (AABB aabb : shape.toAabbs()) {
			Vec3 center = aabb.getCenter();
			settings.addShape(
					(float) center.x, (float) center.y, (float) center.z,
					new BoxShape(
							(float) (aabb.getXsize() / 2),
							(float) (aabb.getYsize() / 2),
							(float) (aabb.getZsize() / 2)
					)
			);
		}

		return settings;
	}
}
