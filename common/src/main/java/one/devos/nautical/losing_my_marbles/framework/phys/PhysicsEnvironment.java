package one.devos.nautical.losing_my_marbles.framework.phys;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import one.devos.nautical.losing_my_marbles.framework.platform.PlatformHelper;

import org.ode4j.math.DVector3C;
import org.ode4j.ode.DBody;
import org.ode4j.ode.DBox;
import org.ode4j.ode.DContact;
import org.ode4j.ode.DContactBuffer;
import org.ode4j.ode.DContactGeomBuffer;
import org.ode4j.ode.DContactJoint;
import org.ode4j.ode.DGeom;
import org.ode4j.ode.DJointGroup;
import org.ode4j.ode.DSpace;
import org.ode4j.ode.DWorld;
import org.ode4j.ode.OdeConstants;
import org.ode4j.ode.OdeHelper;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class PhysicsEnvironment {
	// for some reason this works best with the real gravity value, not in-game ones
	public static final double GRAVITY = -9.81;
	public static final int MAX_CONTACTS = 8;

	private final DWorld world;
	private final DSpace space;

	private final Map<PhysicsEntity, EntityEntry<?>> entities;

	public PhysicsEnvironment(Level level) {
		this.world = OdeHelper.createWorld();
		this.world.setGravity(0, GRAVITY, 0);
		this.world.setDamping(0.01, 0.01);

		this.space = OdeHelper.createHashSpace();
		// constructor registers it
		// new LevelGeom(level, this.space);

		this.entities = new IdentityHashMap<>();
	}

	public static PhysicsEnvironment get(Level level) {
		return PlatformHelper.INSTANCE.getPhysicsEnvironment(level);
	}

	public void entityAdded(Entity entity) {
		if (!(entity instanceof PhysicsEntity physicsEntity))
			return;

		DBody body = OdeHelper.createBody(this.world);
		physicsEntity.buildBody(body);

		body.getGeomIterator().forEachRemaining(this.space::add);

		DVector3C bodyPos = body.getPosition();
		Vec3 entityPos = entity.position();
		Vec3 offset = new Vec3(
				bodyPos.get0() - entityPos.x,
				bodyPos.get1() - entityPos.y,
				bodyPos.get2() - entityPos.z
		);

		this.entities.put(physicsEntity, EntityEntry.create(entity, body, offset));
	}

	public void entityRemoved(Entity entity) {
		if (entity instanceof PhysicsEntity physicsEntity) {
			EntityEntry<?> entry = this.entities.remove(physicsEntity);
			if (entry != null) {
				entry.body().getGeomIterator().forEachRemaining(DGeom::destroy);
			}
		}
	}

	public void tick() {
		if (this.entities.isEmpty()) {
			// nothing to do.
			return;
		}

		// collect surrounding level collision and add it to the space temporarily
		List<DGeom> levelCollision = new ArrayList<>();

		for (EntityEntry<?> entry : this.entities.values()) {
			entry.updateBody();

			this.collectLevelCollision(entry.entity(), levelCollision::add);
		}

		// find collisions
		DContactBuffer contacts = new DContactBuffer(MAX_CONTACTS);
		DContactGeomBuffer geomBuffer = contacts.getGeomBuffer();
		DJointGroup group = OdeHelper.createJointGroup();

		this.space.collide(null, (ignored, g1, g2) -> {
			int contactCount = OdeHelper.collide(g1, g2, MAX_CONTACTS, geomBuffer);
			for (int i = 0; i < contactCount; i++) {
				DContact contact = contacts.get(i);

				contact.surface.mode = OdeConstants.dContactBounce;
				contact.surface.mu = 50;
				contact.surface.soft_erp = 0.96;
				contact.surface.soft_cfm = 2;
				contact.surface.bounce = 0.5;

				DContactJoint joint = OdeHelper.createContactJoint(this.world, group, contact);
				joint.attach(contact.geom.g1.getBody(), contact.geom.g2.getBody());
			}
		});

		// 1 / 20 can be constant here, since changing tick rate changes how often this method is called
		if (!this.world.quickStep(1 / 20f)) {
			throw new RuntimeException("ODE failed to allocate resources to step physics");
		}

		this.entities.values().forEach(EntityEntry::updateEntity);
		levelCollision.forEach(DGeom::destroy);
		group.destroy();
	}

	private void collectLevelCollision(Entity entity, Consumer<DGeom> output) {
		AABB area = entity.getBoundingBox().expandTowards(entity.getDeltaMovement());

		entity.level().getCollisions(entity, area).forEach(shape -> {
			// these shapes are in absolute coords already
			for (AABB box : shape.toAabbs()) {
				DBody boxBody = OdeHelper.createBody(this.world);

				// ignore external forces
				boxBody.setKinematic();

				DBox dBox = OdeHelper.createBox(this.space, box.getXsize(), box.getYsize(), box.getZsize());
				dBox.setBody(boxBody);

				// position in ODE is the center of the box
				Vec3 center = box.getCenter();
				dBox.setPosition(center.x, center.y, center.z);

				output.accept(dBox);
			}
		});
	}
}
