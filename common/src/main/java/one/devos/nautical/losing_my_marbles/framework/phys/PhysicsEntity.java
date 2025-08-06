package one.devos.nautical.losing_my_marbles.framework.phys;

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
	 */
	void buildBody(DBody body);
}
