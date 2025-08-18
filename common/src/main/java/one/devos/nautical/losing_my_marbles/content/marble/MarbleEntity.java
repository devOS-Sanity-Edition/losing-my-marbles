package one.devos.nautical.losing_my_marbles.content.marble;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import com.github.stephengold.joltjni.AaBox;
import com.github.stephengold.joltjni.BodyCreationSettings;
import com.github.stephengold.joltjni.MassProperties;
import com.github.stephengold.joltjni.enumerate.EAllowedDofs;
import com.github.stephengold.joltjni.enumerate.EMotionQuality;
import com.github.stephengold.joltjni.enumerate.EOverrideMassProperties;
import com.github.stephengold.joltjni.readonly.Vec3Arg;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesDataComponents;
import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesEntities;
import one.devos.nautical.losing_my_marbles.content.LosingMyMarblesItemTags;
import one.devos.nautical.losing_my_marbles.content.marble.data.MarbleInstance;
import one.devos.nautical.losing_my_marbles.content.marble.data.shape.MarbleShape;
import one.devos.nautical.losing_my_marbles.content.marble.data.shape.SphereMarbleShape;
import one.devos.nautical.losing_my_marbles.framework.block.MarbleListeningBlock;
import one.devos.nautical.losing_my_marbles.framework.phys.BodyAccess;
import one.devos.nautical.losing_my_marbles.framework.phys.PhysicsEntity;
import one.devos.nautical.losing_my_marbles.framework.phys.core.JoltIntegration;
import one.devos.nautical.losing_my_marbles.framework.phys.core.ObjectLayers;

public final class MarbleEntity extends Entity implements PhysicsEntity, OwnableEntity {
	public static final int HEIGHT_LIMIT_BUFFER = 64;
	public static final float MAX_SCALE = 2;
	public static final float MIN_SCALE = 1 / 2f;

	private final InterpolationHandler interpolator;

	private MarbleInstance marble;

	@Nullable
	private EntityReference<LivingEntity> owner;

	@Nullable
	private BodyAccess body;
	@Nullable
	private Vec3 nextTickPos;
	private double distanceTraveled;

	public MarbleEntity(EntityType<?> type, Level level) {
		this(type, level, MarbleInstance.getDefault(level.registryAccess()));
	}

	public MarbleEntity(EntityType<?> type, Level level, MarbleInstance marble) {
		super(type, level);
		this.blocksBuilding = true;
		// 1 tick: minimal latency for maximum detail
		this.interpolator = new InterpolationHandler(this, 1);
		this.marble = marble;
		this.refreshDimensions();
	}

	public MarbleEntity(Level level, MarbleInstance marble, @Nullable LivingEntity owner) {
		this(LosingMyMarblesEntities.MARBLE, level, marble);
		this.owner = owner == null ? null : new EntityReference<>(owner);
	}

	public MarbleInstance marble() {
		return Objects.requireNonNull(this.marble, "marble() called too early");
	}

	public void setMarble(MarbleInstance marble) {
		this.marble = marble;
		this.updateBody();
		this.refreshDimensions();

		if (this.level() instanceof ServerLevel level) {
			CustomPacketPayload payload = new UpdateMarbleEntityPayload(this.getId(), marble);
			Packet<?> packet = new ClientboundCustomPayloadPacket(payload);
			level.getChunkSource().chunkMap.broadcast(this, packet);
		}
	}

	public double distanceTraveled() {
		return this.distanceTraveled;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
	}

	@Override
	@SuppressWarnings("unchecked")
	public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity serverEntity) {
		return (Packet<ClientGamePacketListener>) (Object) new ClientboundCustomPayloadPacket(new SpawnMarblePayload(
				new ClientboundAddEntityPacket(this, serverEntity), this.marble()
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

		Vec3 oldPos = this.oldPosition();
		double distanceTraveled = oldPos.distanceTo(this.position());
		this.distanceTraveled += distanceTraveled;

		if (this.body != null) {
			this.marble().getOptional(LosingMyMarblesDataComponents.ACCUMULATES_MASS).ifPresent(set -> {
				BlockState ground = this.level().getBlockState(this.getOnPos(0.1f));
				int multiplier = set.contains(ground.getBlockHolder()) ? 1 : -1;

				MarbleInstance newMarble = this.marble().copy();
				float scale = newMarble.getOrDefault(LosingMyMarblesDataComponents.SCALE, 1f);
				float change = multiplier * (float) (distanceTraveled / 20);
				float newScale = Math.clamp(scale + change, MIN_SCALE, MAX_SCALE);
				newMarble.set(LosingMyMarblesDataComponents.SCALE, newScale);
				this.setMarble(newMarble);
			});
		}

		if (this.level() instanceof ServerLevel level) {
			this.applyEffectsFromBlocks();

			BlockPos blockPos = this.blockPosition();
			if (!BlockPos.containing(oldPos).equals(blockPos)) {
				BlockState state = this.getInBlockState();
				if (state.getBlock() instanceof MarbleListeningBlock listening) {
					listening.marbleEntered(level, state, blockPos, this);
				}
			}

			if (this.getY() > level.getMaxY() + HEIGHT_LIMIT_BUFFER) {
				this.discard();
				return;
			}
		}

		this.marble().getOptional(LosingMyMarblesDataComponents.BLOCK_CONTACT_EFFECT).ifPresent(effect -> {
			for (BlockPos pos : BlockPos.betweenClosed(this.getBoundingBox())) {
				BlockState state = this.level().getBlockState(pos);
				effect.apply(this, state, pos);
			}
		});

		this.marble().getOptional(LosingMyMarblesDataComponents.ENTITY_CONTACT_EFFECT).ifPresent(effect -> {
			for (Entity entity : this.level().getEntities(this, this.getBoundingBox(), EntitySelector.CAN_BE_PICKED)) {
				effect.apply(this, entity);
			}
		});
	}

	@Nullable
	@Override
	public InterpolationHandler getInterpolation() {
		return this.interpolator;
	}

	@Nullable
	@Override
	public EntityReference<LivingEntity> getOwnerReference() {
		return this.owner;
	}

	@Override
	public EntityDimensions getDimensions(Pose pose) {
		try (MarbleShape.CreatedShape shape = this.createShape()) {
			try (AaBox box = shape.shape().getLocalBounds()) {
				com.github.stephengold.joltjni.Vec3 size = box.getSize();
				float width = Math.max(size.getX(), size.getZ());
				return EntityDimensions.fixed(width, size.getY());
			}
		}
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

			try (MarbleShape.CreatedShape created = this.createShape()) {
				settings.setShape(created.shape());
				settings.setPosition(JoltIntegration.convert(this.position().add(created.offset())));
			}

			this.marble().getOptional(LosingMyMarblesDataComponents.FRICTION).ifPresent(settings::setFriction);
			this.marble().getOptional(LosingMyMarblesDataComponents.RESTITUTION).ifPresent(settings::setRestitution);
			this.marble().getOptional(LosingMyMarblesDataComponents.GRAVITY_SCALE).ifPresent(settings::setGravityFactor);

			this.marble().getOptional(LosingMyMarblesDataComponents.MASS).ifPresent(mass -> {
				settings.getMassPropertiesOverride().setMass(mass);
				settings.setOverrideMassProperties(EOverrideMassProperties.CalculateInertia);
			});


			this.body = factory.create(settings);
		}
	}

	private void updateBody() {
		BodyAccess body = this.body;
		if (body == null)
			return;

		try (MarbleShape.CreatedShape created = this.createShape()) {
			this.body.setShape(created.shape());
			this.body.setPos(this.position().add(created.offset()));
		}

		this.marble().getOptional(LosingMyMarblesDataComponents.FRICTION).ifPresent(body.getBody()::setFriction);
		this.marble().getOptional(LosingMyMarblesDataComponents.RESTITUTION).ifPresent(body.getBody()::setRestitution);
		this.marble().getOptional(LosingMyMarblesDataComponents.GRAVITY_SCALE).ifPresent(body::setGravityFactor);

		this.marble().getOptional(LosingMyMarblesDataComponents.MASS).ifPresent(mass -> {
			MassProperties properties = new MassProperties();
			properties.setMass(mass);
			body.getBody().getMotionProperties().setMassProperties(EAllowedDofs.All, properties);
		});
	}

	@Override
	public void setNextTickPos(Vec3 pos) {
		this.nextTickPos = pos;
	}

	@Override
	public void onBounce(Vec3 oldVel, Vec3 newVel) {
		this.marble().getOptional(LosingMyMarblesDataComponents.BOUNCE_EFFECT).ifPresent(
				effect -> effect.apply(this, oldVel, newVel)
		);
	}

	@Override
	public boolean isPickable() {
		return !this.isRemoved();
	}

	@Override
	public boolean isPushable() {
		return true;
	}

	@Override
	public void push(double x, double y, double z) {
		if (this.body == null) {
			super.push(x, y, z);
			return;
		}

		com.github.stephengold.joltjni.Vec3 force = new com.github.stephengold.joltjni.Vec3(x, y, z);
		force.scaleInPlace(1000);
		this.body.getBody().addForce(force);
	}

	@Override
	public ItemStack getPickResult() {
		return MarbleItem.of(StoredMarble.of(this.marble()));
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		this.setMarble(input.read("marble", MarbleInstance.CODEC).orElseGet(
				() -> MarbleInstance.getDefault(this.level().registryAccess())
		));
		this.owner = EntityReference.read(input, "owner");
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		output.store("marble", MarbleInstance.CODEC, this.marble());
		EntityReference.store(this.owner, output, "owner");
	}

	@Override
	public InteractionResult interact(Player player, InteractionHand hand) {
		InteractionResult result = super.interact(player, hand);
		if (result.consumesAction()) {
			return result;
		} else if (this.level().isClientSide()) {
			return InteractionResult.SUCCESS;
		}

		ItemStack asItem = this.getPickResult();
		if (!player.addItem(asItem)) {
			player.drop(asItem, false);
		}

		this.discard();
		return InteractionResult.SUCCESS_SERVER;
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		if (!this.isImmuneTo(source)) {
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

		double force = 350;
		ItemStack weapon = source.getWeaponItem();
		if (weapon != null && weapon.is(LosingMyMarblesItemTags.EXTRA_MARBLE_KNOCKBACK)) {
			force *= 2;
		}

		Vec3Arg forceVec = JoltIntegration.convertF(pos.vectorTo(this.position()).normalize().scale(force));
		this.body.getBody().addForce(forceVec);
		return true;
	}

	private boolean isImmuneTo(DamageSource source) {
		if (source.getDirectEntity() instanceof Player)
			return true;

		return this.marble().getOptional(LosingMyMarblesDataComponents.DAMAGE_IMMUNE)
				.map(set -> set.contains(source.typeHolder()))
				.orElse(false);
	}

	private MarbleShape.CreatedShape createShape() {
		float scale = this.marble().getOrDefault(LosingMyMarblesDataComponents.SCALE, 1f);
		MarbleShape shape = this.marble().getOrDefault(LosingMyMarblesDataComponents.SHAPE, SphereMarbleShape.DEFAULT);
		return shape.createJoltShape(scale);
	}
}
