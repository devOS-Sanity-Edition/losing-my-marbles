package one.devos.nautical.losing_my_marbles.framework.phys;

import com.github.stephengold.joltjni.Body;

import com.github.stephengold.joltjni.BodyInterface;

import com.github.stephengold.joltjni.enumerate.EActivation;

import com.github.stephengold.joltjni.readonly.ConstBodyCreationSettings;

import net.minecraft.world.phys.Vec3;
import one.devos.nautical.losing_my_marbles.framework.phys.core.JoltIntegration;

public sealed interface BodyAccess {
	Body getBody();

	void setPos(Vec3 pos);

	void setVelocity(Vec3 velocity);

	void discard();

	interface Factory {
		BodyAccess create(ConstBodyCreationSettings settings);
	}

	// getId is not cached, that's a lot of JNI!
	record Impl(int id, Body body, BodyInterface bodies) implements BodyAccess {
		Impl(Body body, BodyInterface bodies) {
			this(body.getId(), body, bodies);
		}

		@Override
		public Body getBody() {
			return this.body;
		}

		@Override
		public void setPos(Vec3 pos) {
			this.bodies.setPosition(this.id, JoltIntegration.convert(pos), EActivation.DontActivate);
		}

		@Override
		public void setVelocity(Vec3 velocity) {
			this.bodies.setLinearVelocity(this.id, JoltIntegration.convertF(velocity));
		}

		@Override
		public void discard() {
			this.bodies.removeBody(this.id);
			this.bodies.destroyBody(this.id);
		}
	}
}
