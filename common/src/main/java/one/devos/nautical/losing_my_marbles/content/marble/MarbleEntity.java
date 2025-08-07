package one.devos.nautical.losing_my_marbles.content.marble;

import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import one.devos.nautical.losing_my_marbles.framework.phys.PhysicsEntity;

import org.jetbrains.annotations.Nullable;

public final class MarbleEntity extends Entity implements PhysicsEntity {
	public static final float RADIUS = 3 / 16f;
	public static final float DIAMETER = RADIUS * 2;

	// @Nullable
	// private DBody body;
	@Nullable
	private Vec3 nextTickPos;

	public MarbleEntity(EntityType<?> type, Level level) {
		super(type, level);
		this.blocksBuilding = true;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
	}

	@Override
	public void tick() {
		super.tick();
		if (this.nextTickPos != null) {
			this.setPos(this.nextTickPos);
			this.nextTickPos = null;
		}
	}

	// @Override
	// public void buildBody(DBody body) {
	// 	DSphere geometry = OdeHelper.createSphere(RADIUS);
	// 	geometry.setBody(body);
	//
	// 	DMass mass = OdeHelper.createMass();
	// 	mass.setSphereTotal(10, RADIUS);
	// 	body.setMass(mass);
	//
	// 	body.setPosition(this.getX(), this.getY() + RADIUS, this.getZ());
	// 	Vec3 vel = this.getDeltaMovement();
	// 	body.setLinearVel(vel.x, vel.y, vel.z);
	//
	// 	this.body = body;
	// }

	@Override
	public void setNextTickPos(Vec3 pos) {
		this.nextTickPos = pos;
	}

	@Override
	public boolean isPickable() {
		return !this.isRemoved();
	}

	public Vec3 center() {
		return this.position().add(0, RADIUS, 0);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		// if (source.getDirectEntity() instanceof Player player && player.isSecondaryUseActive()) {
		// 	// TODO: store and check for owner, drop item
		// 	this.discard();
			return true;
		// }

		// apply knockback

		// if (this.body == null)
		// 	return false;
		//
		// Vec3 pos = source.getSourcePosition();
		// if (pos == null) {
		// 	return false;
		// }
		//
		// Vec3 force = pos.vectorTo(this.center()).normalize().scale(500);
		// this.body.addForce(force.x, force.y, force.z);
		// return true;
	}
}
