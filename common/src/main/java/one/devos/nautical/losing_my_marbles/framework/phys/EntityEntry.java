package one.devos.nautical.losing_my_marbles.framework.phys;

import com.github.stephengold.joltjni.RVec3;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public record EntityEntry<T extends Entity & PhysicsEntity>(T entity, BodyAccess body, Vec3 offsetToCenterOfMass) {
	/**
	 * Area around this entity where terrain should be added to the system.
	 */
	public AABB terrainBounds() {
		return this.entity.getBoundingBox().expandTowards(this.entity.getDeltaMovement().scale(5));
	}

	public void updateBody() {
		Vec3 pos = this.entity.position();
		this.body.setPos(pos.add(this.offsetToCenterOfMass));

		// scale from m/t to m/s
		Vec3 vel = this.entity.getDeltaMovement().scale(20);
		this.body.setVelocity(vel);
	}

	public void updateEntity() {
		if (this.entity.getVehicle() != null)
			return;

		RVec3 pos = this.body.getBody().getPosition();
		this.entity.setNextTickPos(new Vec3(
				pos.xx() - this.offsetToCenterOfMass.x,
				pos.yy() - this.offsetToCenterOfMass.y,
				pos.zz() - this.offsetToCenterOfMass.z
		));

		com.github.stephengold.joltjni.Vec3 vel = this.body.getBody().getLinearVelocity();
		// scale from m/s to m/t
		vel.scaleInPlace(1 / 20f);

		Vec3 oldVel = this.entity.getDeltaMovement();
		if (oldVel.x != vel.getX() || oldVel.y != vel.getY() || oldVel.z != vel.getZ()) {
			Vec3 newVel = new Vec3(vel.getX(), vel.getY(), vel.getZ());

			this.entity.setDeltaMovement(newVel);
			this.entity.hasImpulse = true;

			if (oldVel.lengthSqr() > 0.01 && angleBetween(oldVel, newVel) > 20) {
				this.entity.onBounce(oldVel, newVel);
			}
		}
	}

	public void close() {
		this.body.discard();
	}

	@SuppressWarnings("unchecked")
	public static <T extends Entity & PhysicsEntity> EntityEntry<?> create(Entity entity, BodyAccess body, Vec3 offset) {
		if (!(entity instanceof PhysicsEntity)) {
			throw new IllegalArgumentException("Entity is not a PhysicsEntity: " + entity);
		}

		return new EntityEntry<>((T) entity, body, offset);
	}

	private static double angleBetween(Vec3 a, Vec3 b) {
		return Mth.RAD_TO_DEG * Math.acos(a.dot(b) / (a.length() * b.length()));
	}
}
