package one.devos.nautical.losing_my_marbles.content.marble;

import org.jetbrains.annotations.Nullable;

import com.github.stephengold.joltjni.BodyCreationSettings;
import com.github.stephengold.joltjni.enumerate.EMotionQuality;
import com.github.stephengold.joltjni.enumerate.EOverrideMassProperties;
import com.github.stephengold.joltjni.readonly.Vec3Arg;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
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
import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesDataComponents;
import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesEntities;
import one.devos.nautical.losing_my_marbles.content.marble.data.MarbleInstance;
import one.devos.nautical.losing_my_marbles.content.marble.data.shape.MarbleShape;
import one.devos.nautical.losing_my_marbles.content.marble.data.shape.SphereMarbleShape;
import one.devos.nautical.losing_my_marbles.framework.phys.BodyAccess;
import one.devos.nautical.losing_my_marbles.framework.phys.PhysicsEntity;
import one.devos.nautical.losing_my_marbles.framework.phys.core.JoltIntegration;
import one.devos.nautical.losing_my_marbles.framework.phys.core.ObjectLayers;

public final class MarbleEntity extends Entity implements PhysicsEntity {
	// TODO: this is h
	public static final float DIAMETER = SphereMarbleShape.DEFAULT.radius();

	private final InterpolationHandler interpolator;

	private MarbleInstance marble;

	@Nullable
	private BodyAccess body;
	@Nullable
	private Vec3 nextTickPos;

	public MarbleEntity(EntityType<?> type, Level level) {
		this(type, level, MarbleInstance.getDefault(level.registryAccess()));
	}

	public MarbleEntity(EntityType<?> type, Level level, MarbleInstance marble) {
		super(type, level);
		this.blocksBuilding = true;
		// 1 tick: minimal latency for maximum detail
		this.interpolator = new InterpolationHandler(this, 1);
		this.marble = marble;
	}

	public MarbleEntity(Level level, MarbleInstance marble) {
		this(LosingMyMarblesEntities.MARBLE, level, marble);
	}

	public MarbleInstance marble() {
		return this.marble;
	}

	public void setMarble(MarbleInstance marble) {
		this.marble = marble;

		if (this.level() instanceof ServerLevel level) {
			CustomPacketPayload payload = new UpdateMarbleEntityPayload(this.getId(), marble);
			Packet<?> packet = new ClientboundCustomPayloadPacket(payload);
			level.getChunkSource().chunkMap.broadcast(this, packet);
		}
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
	}

	@Override
	@SuppressWarnings("unchecked")
	public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity serverEntity) {
		return (Packet<ClientGamePacketListener>) (Object) new ClientboundCustomPayloadPacket(new SpawnMarblePayload(
				new ClientboundAddEntityPacket(this, serverEntity),
				this.marble
		));
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
			settings.setLinearDamping(0.2f);

			Vec3 vel = this.getDeltaMovement();
			settings.setLinearVelocity((float) vel.x, (float) vel.y, (float) vel.z);

			// prevent tunneling
			settings.setMotionQuality(EMotionQuality.LinearCast);

			float scale = this.marble.getOrDefault(LosingMyMarblesDataComponents.SCALE, 1f);
			MarbleShape shape = this.marble.getOrDefault(LosingMyMarblesDataComponents.SHAPE, SphereMarbleShape.DEFAULT);
			try (MarbleShape.CreatedShape created = shape.createJoltShape(scale)) {
				settings.setShape(created.shape());
				settings.setPosition(
						this.getX() + created.offset().getX(),
						this.getY() + created.offset().getY(),
						this.getZ() + created.offset().getZ()
				);
			}

			this.marble.getOptional(LosingMyMarblesDataComponents.FRICTION).ifPresent(settings::setFriction);
			this.marble.getOptional(LosingMyMarblesDataComponents.RESTITUTION).ifPresent(settings::setRestitution);

			this.marble.getOptional(LosingMyMarblesDataComponents.MASS).ifPresent(mass -> {
				settings.getMassPropertiesOverride().setMass(mass);
				settings.setOverrideMassProperties(EOverrideMassProperties.CalculateInertia);
			});

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

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		this.marble = input.read("marble", MarbleInstance.CODEC).orElseGet(
				() -> MarbleInstance.getDefault(this.level().registryAccess())
		);
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		output.store("marble", MarbleInstance.CODEC, this.marble);
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

		Vec3Arg force = JoltIntegration.convertF(pos.vectorTo(this.position()).normalize().scale(500));
		this.body.getBody().addForce(force);
		return true;
	}
}
