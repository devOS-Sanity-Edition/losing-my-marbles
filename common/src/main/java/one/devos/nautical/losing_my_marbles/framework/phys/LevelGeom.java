package one.devos.nautical.losing_my_marbles.framework.phys;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.level.BlockCollisions;
import net.minecraft.world.level.Level;

import net.minecraft.world.phys.AABB;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.ode4j.ode.DAABB;
import org.ode4j.ode.DAABBC;
import org.ode4j.ode.DBox;
import org.ode4j.ode.DContactGeomBuffer;
import org.ode4j.ode.DGeom;
import org.ode4j.ode.DSpace;
import org.ode4j.ode.DSphere;
import org.ode4j.ode.OdeHelper;
import org.ode4j.ode.internal.DxGeom;
import org.ode4j.ode.internal.DxSpace;

public final class LevelGeom extends DxGeom {
	public static final int CLASS = dFirstUserClass + 1;
	public static final DAABBC LEVEL_BOUNDS = new DAABB(-30_000_000, 30_000_000, -2048, 2048, -30_000_000, 30_000_000);

	public final Level level;

	public LevelGeom(Level level, DSpace space) {
		super((DxSpace) space, false);
		this.type = CLASS;

		this.level = level;
	}

	@Override
	protected void computeAABB() {
		this._aabb.set(LEVEL_BOUNDS);
	}

	public static int collide(DGeom g1, DGeom g2, int flags, DContactGeomBuffer contacts) {
		if (!(g1 instanceof DSphere sphere) || !(g2 instanceof LevelGeom levelGeom)) {
			throw new IllegalArgumentException("LevelGeom.collide only handles Sphere/LevelGeom");
		}

		int maxContacts = flags & NUMC_MASK;
		if (maxContacts == 0) {
			throw new IllegalArgumentException("maxContacts must be >0");
		}

		DAABBC sphereBounds = sphere.getAABB();
		AABB area = new AABB(
				sphereBounds.getMin0(), sphereBounds.getMin1(), sphereBounds.getMin2(),
				sphereBounds.getMax0(), sphereBounds.getMax1(), sphereBounds.getMax2()
		);

		int contactCount = 0;
		DBox reusableBox = OdeHelper.createBox(0, 0, 0);

		for (Block block : collidingBlocks(levelGeom.level, area)) {
			for (AABB aabb : block.shape.toAabbs()) {
				updateBox(reusableBox, aabb);
				DContactGeomBuffer subBuffer = contacts.createView(contactCount);

				contactCount += OdeHelper.collide(sphere, reusableBox, 1, subBuffer);

				if (contactCount >= maxContacts) {
					return contactCount;
				}
			}
		}

		for (Entity entity : levelGeom.level.getEntities((Entity) null, area, EntitySelector.CAN_BE_COLLIDED_WITH)) {
			updateBox(reusableBox, entity.getBoundingBox());
			DContactGeomBuffer subBuffer = contacts.createView(contactCount);

			contactCount += OdeHelper.collide(sphere, reusableBox, 1, subBuffer);

			if (contactCount >= maxContacts) {
				return contactCount;
			}
		}

		return contactCount;
	}

	public static void init() {
		OdeHelper.setColliderOverride(dSphereClass, CLASS, LevelGeom::collide);
	}

	private static Iterable<Block> collidingBlocks(Level level, AABB area) {
		Block holder = new Block();
		return () -> new BlockCollisions<>(level, CollisionContext.empty(), area, false, (pos, shape) -> {
			holder.pos = pos;
			holder.shape = shape;
			return holder;
		});
	}

	private static void updateBox(DBox box, AABB aabb) {
		box.setLengths(aabb.getXsize(), aabb.getYsize(), aabb.getZsize());
		Vec3 center = aabb.getCenter();
		box.setPosition(center.x, center.y, center.z);
	}

	private static final class Block {
		private BlockPos.MutableBlockPos pos;
		private VoxelShape shape;
	}
}
