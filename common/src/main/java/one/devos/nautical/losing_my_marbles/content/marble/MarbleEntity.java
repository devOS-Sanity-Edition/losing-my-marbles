package one.devos.nautical.losing_my_marbles.content.marble;

import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import one.devos.nautical.losing_my_marbles.framework.phys.PhysicsEntity;

import org.ode4j.ode.DBody;
import org.ode4j.ode.DMass;
import org.ode4j.ode.DSphere;
import org.ode4j.ode.OdeHelper;

public final class MarbleEntity extends Entity implements PhysicsEntity {
	public static final float RADIUS = 3 / 16f;
	public static final float DIAMETER = RADIUS * 2;

	public MarbleEntity(EntityType<?> type, Level level) {
		super(type, level);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
	}

	@Override
	public void buildBody(DBody body) {
		DSphere geometry = OdeHelper.createSphere(RADIUS);
		geometry.setBody(body);

		DMass mass = OdeHelper.createMass();
		mass.setSphereTotal(10, RADIUS);
		body.setMass(mass);

		body.setPosition(this.getX(), this.getY() + RADIUS, this.getZ());
		Vec3 vel = this.getDeltaMovement();
		body.setLinearVel(vel.x, vel.y, vel.z);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		return false;
	}
}
