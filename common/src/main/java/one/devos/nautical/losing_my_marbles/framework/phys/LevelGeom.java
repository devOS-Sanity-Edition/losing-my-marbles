package one.devos.nautical.losing_my_marbles.framework.phys;

import net.minecraft.world.level.Level;

import org.ode4j.ode.DAABB;
import org.ode4j.ode.DAABBC;
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

		Level level = levelGeom.level;

		// h
		return 0;
	}

	public static void init() {
		OdeHelper.setColliderOverride(dSphereClass, CLASS, LevelGeom::collide);
	}
}
