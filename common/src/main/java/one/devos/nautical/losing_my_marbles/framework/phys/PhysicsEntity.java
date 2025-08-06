package one.devos.nautical.losing_my_marbles.framework.phys;

import net.minecraft.world.phys.Vec3;

import org.ode4j.ode.DBody;

/**
 * An entity that has a physics body representation.
 * <p>
 * Position and velocity will be automatically synced back and forth between the entity and its body.
 */
public interface PhysicsEntity {
	/**
	 * Modify the given body to match this entity.
	 * Should set geometry, position, velocity, and anything else important.
	 * <p>
	 * It is safe to hold on to the given body to apply interactions.
	 */
	void buildBody(DBody body);

	/**
	 * Set the position that the entity should move to the next time it ticks.
	 */
	void setNextTickPos(Vec3 pos);
}
