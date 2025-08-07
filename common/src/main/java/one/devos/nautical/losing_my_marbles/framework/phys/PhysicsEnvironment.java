package one.devos.nautical.losing_my_marbles.framework.phys;

import com.github.stephengold.joltjni.AaBox;
import com.github.stephengold.joltjni.Body;
import com.github.stephengold.joltjni.BodyInterface;
import com.github.stephengold.joltjni.BroadPhaseLayerFilter;
import com.github.stephengold.joltjni.JobSystem;
import com.github.stephengold.joltjni.JobSystemSingleThreaded;
import com.github.stephengold.joltjni.ObjectLayerFilter;
import com.github.stephengold.joltjni.PhysicsSystem;

import com.github.stephengold.joltjni.RVec3;
import com.github.stephengold.joltjni.TempAllocator;
import com.github.stephengold.joltjni.TempAllocatorMalloc;
import com.github.stephengold.joltjni.enumerate.EActivation;

import com.github.stephengold.joltjni.enumerate.EPhysicsUpdateError;

import com.github.stephengold.joltjni.readonly.Vec3Arg;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import one.devos.nautical.losing_my_marbles.framework.phys.core.JoltIntegration;
import one.devos.nautical.losing_my_marbles.framework.platform.PlatformHelper;

import org.apache.commons.lang3.mutable.MutableObject;

import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

public final class PhysicsEnvironment {
	// this can be constant, since changing tick rate changes how often the method is called
	public static final float TIME_STEP = 1 / 20f;
	// for some reason this works best with the real gravity value, not in-game ones
	public static final float GRAVITY = -9.81f;

	// padding around an object's bounding box to check for adjacent objects for reactivation
	public static final Vec3Arg REACTIVATION_EXPANSION = new com.github.stephengold.joltjni.Vec3(0.1, 0.1, 0.1);

	public static final BroadPhaseLayerFilter DUMMY_BROAD_PHASE_FILTER = new BroadPhaseLayerFilter();
	public static final ObjectLayerFilter DUMMY_OBJECT_LAYER_FILTER = new ObjectLayerFilter();

	private final Level level;

	private final TempAllocator tempAllocator;
	private final JobSystem jobSystem;
	private final PhysicsSystem system;
	private final BodyInterface bodies;

	private final Map<PhysicsEntity, EntityEntry<?>> entities;

	public PhysicsEnvironment(Level level) {
		this.level = level;

		this.tempAllocator = new TempAllocatorMalloc();
		this.jobSystem = new JobSystemSingleThreaded(2048);

		this.system = JoltIntegration.createSystem();
		this.system.setGravity(0, GRAVITY, 0);

		this.bodies = this.system.getBodyInterfaceNoLock();

		this.entities = new IdentityHashMap<>();
	}

	public static PhysicsEnvironment get(Level level) {
		return PlatformHelper.INSTANCE.getPhysicsEnvironment(level);
	}

	public void entityAdded(Entity entity) {
		if (!(entity instanceof PhysicsEntity physicsEntity))
			return;

		MutableObject<BodyAccess> bodyHolder = new MutableObject<>();
		physicsEntity.createBody(settings -> {
			if (bodyHolder.getValue() != null) {
				throw new IllegalStateException("Multiple bodies on 1 PhysicsEntity is not supported (yet?)");
			}

			Body body = this.bodies.createBody(settings);
			this.bodies.addBody(body.getId(), EActivation.Activate);
			BodyAccess access = new BodyAccess.Impl(body, this.bodies);
			bodyHolder.setValue(access);
			return access;
		});

		BodyAccess body = bodyHolder.getValue();
		if (body == null)
			return;

		RVec3 bodyPos = body.getBody().getPosition();
		Vec3 entityPos = entity.position();
		Vec3 offset = new Vec3(
				bodyPos.xx() - entityPos.x,
				bodyPos.yy() - entityPos.y,
				bodyPos.zz() - entityPos.z
		);

		this.entities.put(physicsEntity, EntityEntry.create(entity, body, offset));
	}

	public void entityRemoved(Entity entity) {
		if (entity instanceof PhysicsEntity physicsEntity) {
			EntityEntry<?> entry = this.entities.remove(physicsEntity);
			if (entry != null) {
				// need to reactivate any adjacent objects, that's not automatic
				// no reason to make a copy, it's a fresh object, save an allocation
				AaBox bounds = (AaBox) entry.body().getBody().getWorldSpaceBounds();
				bounds.expandBy(REACTIVATION_EXPANSION);

				entry.body().discard();

				this.bodies.activateBodiesInAaBox(bounds, DUMMY_BROAD_PHASE_FILTER, DUMMY_OBJECT_LAYER_FILTER);
			}
		}
	}

	public void tick() {
		if (this.entities.isEmpty()) {
			// nothing to do.
			return;
		}

		this.entities.values().forEach(EntityEntry::updateBody);

		int errors = this.system.update(TIME_STEP, 1, this.tempAllocator, this.jobSystem);
		if (errors != 0) {
			throw new RuntimeException("Error(s) occurred while updating physics: " + Error.setOf(errors));
		}

		this.entities.values().forEach(EntityEntry::updateEntity);
	}

	private enum Error {
		MANIFOLD_CACHE_FULL(EPhysicsUpdateError.ManifoldCacheFull),
		BODY_PAIR_CACHE_FULL(EPhysicsUpdateError.BodyPairCacheFull),
		CONTACT_CONSTRAINTS_FULL(EPhysicsUpdateError.ContactConstraintsFull);

		private final int code;

		Error(int code) {
			this.code = code;
		}

		private static Set<Error> setOf(int flags) {
			Set<Error> set = EnumSet.noneOf(Error.class);

			for (Error error : Error.values()) {
				if ((flags & error.code) != 0) {
					set.add(error);
				}
			}

			return set;
		}
	}
}
