package one.devos.nautical.losing_my_marbles.framework.phys;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

// public record EntityEntry<T extends Entity & PhysicsEntity>(T entity, DBody body, Vec3 offsetToCenterOfMass) {
// 	public void updateBody() {
// 		Vec3 pos = this.entity.position();
// 		this.body.setPosition(
// 				pos.x + this.offsetToCenterOfMass.x,
// 				pos.y + this.offsetToCenterOfMass.y,
// 				pos.z + this.offsetToCenterOfMass.z
// 		);
//
// 		Vec3 vel = this.entity.getDeltaMovement();
// 		this.body.setLinearVel(vel.x, vel.y, vel.z);
// 	}
//
// 	public void updateEntity() {
// 		if (this.entity.getVehicle() != null)
// 			return;
//
// 		DVector3C pos = this.body.getPosition();
// 		this.entity.setNextTickPos(new Vec3(
// 				pos.get0() - this.offsetToCenterOfMass.x,
// 				pos.get1() - this.offsetToCenterOfMass.y,
// 				pos.get2() - this.offsetToCenterOfMass.z
// 		));
//
// 		DVector3C vel = this.body.getLinearVel();
// 		Vec3 oldVel = this.entity.getDeltaMovement();
// 		if (oldVel.x != vel.get0() || oldVel.y != vel.get1() || oldVel.z != vel.get2()) {
// 			this.entity.setDeltaMovement(vel.get0(), vel.get1(), vel.get2());
// 			this.entity.hasImpulse = true;
// 		}
// 	}
//
// 	@SuppressWarnings("unchecked")
// 	public static <T extends Entity & PhysicsEntity> EntityEntry<?> create(Entity entity, DBody body, Vec3 offset) {
// 		if (!(entity instanceof PhysicsEntity)) {
// 			throw new IllegalArgumentException("Entity is not a PhysicsEntity: " + entity);
// 		}
//
// 		return new EntityEntry<>((T) entity, body, offset);
// 	}
// }
