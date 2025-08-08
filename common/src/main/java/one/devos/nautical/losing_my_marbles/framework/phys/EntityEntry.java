package one.devos.nautical.losing_my_marbles.framework.phys;

import com.github.stephengold.joltjni.RVec3;

import com.github.stephengold.joltjni.readonly.Vec3Arg;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public record EntityEntry<T extends Entity & PhysicsEntity>(T entity, BodyAccess body, Vec3 offsetToCenterOfMass) {
	/**
	 * Area around this entity where terrain should be added to the system.
	 */
	public AABB terrainBounds() {
		return this.entity.getBoundingBox().expandTowards(this.entity.getDeltaMovement());
	}

	public void updateBody() {
		Vec3 pos = this.entity.position();
		this.body.setPos(pos.add(this.offsetToCenterOfMass));

		Vec3 vel = this.entity.getDeltaMovement();
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

		Vec3Arg vel = this.body.getBody().getLinearVelocity();
		Vec3 oldVel = this.entity.getDeltaMovement();
		if (oldVel.x != vel.getX() || oldVel.y != vel.getY() || oldVel.z != vel.getZ()) {
			this.entity.setDeltaMovement(vel.getX(), vel.getY(), vel.getZ());
			this.entity.hasImpulse = true;
		}
	}

	@SuppressWarnings("unchecked")
	public static <T extends Entity & PhysicsEntity> EntityEntry<?> create(Entity entity, BodyAccess body, Vec3 offset) {
		if (!(entity instanceof PhysicsEntity)) {
			throw new IllegalArgumentException("Entity is not a PhysicsEntity: " + entity);
		}

		return new EntityEntry<>((T) entity, body, offset);
	}
}
