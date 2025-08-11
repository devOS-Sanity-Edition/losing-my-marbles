package one.devos.nautical.losing_my_marbles.content.marble;

import com.github.stephengold.joltjni.BodyCreationSettings;

import com.github.stephengold.joltjni.SphereShape;

import com.github.stephengold.joltjni.enumerate.EMotionQuality;
import com.github.stephengold.joltjni.enumerate.EOverrideMassProperties;
import com.github.stephengold.joltjni.readonly.Vec3Arg;

import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import one.devos.nautical.losing_my_marbles.framework.phys.BodyAccess;
import one.devos.nautical.losing_my_marbles.framework.phys.PhysicsEntity;

import one.devos.nautical.losing_my_marbles.framework.phys.core.JoltIntegration;

import one.devos.nautical.losing_my_marbles.framework.phys.core.ObjectLayers;

import org.jetbrains.annotations.Nullable;

public final class MarbleEntity extends Entity implements PhysicsEntity {
	public static final float RADIUS = 3 / 16f;
	public static final float DIAMETER = RADIUS * 2;

	private final InterpolationHandler interpolator;

	@Nullable
	private BodyAccess body;
	@Nullable
	private Vec3 nextTickPos;

	public MarbleEntity(EntityType<?> type, Level level) {
		super(type, level);
		this.blocksBuilding = true;
		this.interpolator = new InterpolationHandler(this);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
	}

	@Override
	public void tick() {
		super.tick();
		this.interpolator.interpolate();

		if (this.nextTickPos != null) {
			this.setPos(this.nextTickPos);
			this.nextTickPos = null;
		}
	}

	@Nullable
	@Override
	public InterpolationHandler getInterpolation() {
		return this.interpolator;
	}

	@Override
	public void createBody(BodyAccess.Factory factory) {
		try (BodyCreationSettings settings = new BodyCreationSettings()) {
			settings.setObjectLayer(ObjectLayers.MOVING);
			settings.setShape(new SphereShape(RADIUS));

			settings.setPosition(this.getX(), this.getY() + RADIUS, this.getZ());

			Vec3 vel = this.getDeltaMovement();
			settings.setLinearVelocity((float) vel.x, (float) vel.y, (float) vel.z);

			// 10kg, calculate inertia from mass + shape
			settings.getMassPropertiesOverride().setMass(10);
			settings.setOverrideMassProperties(EOverrideMassProperties.CalculateInertia);
			// 50% bouncy
			settings.setRestitution(0.5f);
			// prevent tunneling
			settings.setMotionQuality(EMotionQuality.LinearCast);

			this.body = factory.create(settings);
		}
	}

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
		if (source.getDirectEntity() instanceof Player player && player.isSecondaryUseActive()) {
			// TODO: store and check for owner, drop item
			this.discard();
			return true;
		}

		// apply knockback

		if (this.body == null)
			return false;

		Vec3 pos = source.getSourcePosition();
		if (pos == null) {
			return false;
		}

		Vec3Arg force = JoltIntegration.convertF(pos.vectorTo(this.center()).normalize().scale(500));
		this.body.getBody().addForce(force);
		return true;
	}
}
