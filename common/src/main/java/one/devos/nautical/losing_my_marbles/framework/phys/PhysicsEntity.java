package one.devos.nautical.losing_my_marbles.framework.phys;

import net.minecraft.world.phys.Vec3;

/**
 * An entity that has a physics body representation.
 * <p>
 * Position and velocity will be automatically synced back and forth between the entity and its body.
 */
public interface PhysicsEntity {
	/**
	 * Create this entity's physics body.
	 * Should set geometry, position, velocity, and anything else important.
	 * <p>
	 * The created body can be saved for later to apply interactions.
	 */
	void createBody(BodyAccess.Factory factory);

	/**
	 * Set the position that the entity should move to the next time it ticks.
	 */
	void setNextTickPos(Vec3 pos);
}
