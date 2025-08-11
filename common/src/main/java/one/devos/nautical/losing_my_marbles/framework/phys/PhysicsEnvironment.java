package one.devos.nautical.losing_my_marbles.framework.phys;

import com.github.stephengold.joltjni.AaBox;
import com.github.stephengold.joltjni.Body;
import com.github.stephengold.joltjni.BodyCreationSettings;
import com.github.stephengold.joltjni.BodyIdArray;
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

import com.github.stephengold.joltjni.enumerate.EMotionType;
import com.github.stephengold.joltjni.enumerate.EPhysicsUpdateError;

import com.github.stephengold.joltjni.readonly.ConstAaBox;
import com.github.stephengold.joltjni.readonly.Vec3Arg;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import one.devos.nautical.losing_my_marbles.framework.phys.core.JoltIntegration;
import one.devos.nautical.losing_my_marbles.framework.phys.core.ObjectLayers;
import one.devos.nautical.losing_my_marbles.framework.phys.terrain.BoxCache;
import one.devos.nautical.losing_my_marbles.framework.phys.terrain.CompileTask;
import one.devos.nautical.losing_my_marbles.framework.phys.terrain.CompiledSection;
import one.devos.nautical.losing_my_marbles.framework.phys.terrain.SectionShapeCompiler;
import one.devos.nautical.losing_my_marbles.framework.platform.PlatformHelper;

import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class PhysicsEnvironment {
	// this can be constant, since changing tick rate changes how often the method is called
	public static final float TIME_STEP = 1 / 20f;
	// for some reason this works best with the real gravity value, not in-game ones
	public static final float GRAVITY = -9.81f;

	// padding around an object's bounding box to check for adjacent objects for reactivation
	public static final Vec3Arg REACTIVATION_EXPANSION = new com.github.stephengold.joltjni.Vec3(0.1, 0.1, 0.1);

	public static final BroadPhaseLayerFilter DUMMY_BROAD_PHASE_FILTER = new BroadPhaseLayerFilter();
	public static final ObjectLayerFilter DUMMY_OBJECT_LAYER_FILTER = new ObjectLayerFilter();

	private final ServerLevel level;
	private final Executor executor;

	private final TempAllocator tempAllocator;
	private final JobSystem jobSystem;
	private final PhysicsSystem system;
	private final BodyInterface bodies;

	private final BoxCache boxCache;
	private final Long2ObjectMap<List<BodyAccess>> chunkSectionBodies;
	private final Long2ObjectMap<CompileTask> compilingSections;

	private final Map<PhysicsEntity, EntityEntry<?>> entities;

	public PhysicsEnvironment(ServerLevel level) {
		this.level = level;
		ResourceLocation dim = level.dimension().location();
		this.executor = Util.backgroundExecutor().forName("PhysicsEnvironment/" + dim);

		this.tempAllocator = new TempAllocatorMalloc();
		this.jobSystem = new JobSystemSingleThreaded(2048);

		this.system = JoltIntegration.createSystem();
		this.system.setGravity(0, GRAVITY, 0);

		this.bodies = this.system.getBodyInterfaceNoLock();

		this.boxCache = new BoxCache();
		this.chunkSectionBodies = new Long2ObjectOpenHashMap<>();
		this.compilingSections = new Long2ObjectOpenHashMap<>();

		this.entities = new IdentityHashMap<>();
	}

	public static PhysicsEnvironment get(ServerLevel level) {
		return PlatformHelper.INSTANCE.getPhysicsEnvironment(level);
	}

	public void close() {
		this.chunkSectionBodies.values().stream()
				.flatMap(Collection::stream)
				.forEach(BodyAccess::discard);

		this.compilingSections.values().forEach(CompileTask::discardResult);

		this.entities.values().forEach(EntityEntry::close);

		this.system.close();
		this.tempAllocator.close();
		this.jobSystem.close();
		this.boxCache.close();
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
				ConstAaBox bounds = entry.body().getBody().getWorldSpaceBounds();
				try (AaBox copy = new AaBox(bounds)) {
					copy.expandBy(REACTIVATION_EXPANSION);

					entry.body().discard();
					this.wakeWithin(bounds);
				}
			}
		}
	}

	public void tick() {
		// add any chunk sections that have finished compiling off-thread
		this.pollSectionCompiles();

		if (this.entities.isEmpty()) {
			// nothing else to do.
			return;
		}

		// sync each body with its entity
		this.entities.values().forEach(EntityEntry::updateBody);

		int errors = this.system.update(TIME_STEP, 1, this.tempAllocator, this.jobSystem);
		if (errors != 0) {
			throw new RuntimeException("Error(s) occurred while updating physics: " + Error.setOf(errors));
		}

		// sync updated bodies back to their entities
		this.entities.values().forEach(EntityEntry::updateEntity);
	}

	private void pollSectionCompiles() {
		if (this.compilingSections.isEmpty())
			return;

		List<BodyAccess> newSectionBodies = new ArrayList<>();
		ObjectIterator<Long2ObjectMap.Entry<CompileTask>> itr = this.compilingSections.long2ObjectEntrySet().iterator();

		while (itr.hasNext()) {
			Long2ObjectMap.Entry<CompileTask> entry = itr.next();
			CompileTask task = entry.getValue();

			if (!task.future().isDone())
				continue;

			CompiledSection compiled = task.future().join();

			long pos = entry.getLongKey();
			int posX = SectionPos.sectionToBlockCoord(SectionPos.x(pos));
			int posY = SectionPos.sectionToBlockCoord(SectionPos.y(pos));
			int posZ = SectionPos.sectionToBlockCoord(SectionPos.z(pos));

			List<BodyAccess> bodies = compiled.shapes().stream().map(shape -> {
				try (BodyCreationSettings settings = new BodyCreationSettings()) {
					shape.configure(settings);
					settings.setObjectLayer(ObjectLayers.STATIC);
					settings.setEnhancedInternalEdgeRemoval(true);
					settings.setMotionType(EMotionType.Static);
					settings.setPosition(posX, posY, posZ);

					Body body = this.bodies.createBody(settings);
					// this cast is stupid
					return (BodyAccess) new BodyAccess.Impl(body, this.bodies);
				}
			}).toList();

			newSectionBodies.addAll(bodies);
			List<BodyAccess> oldBodies = this.chunkSectionBodies.put(pos, bodies);
			if (oldBodies != null) {
				oldBodies.forEach(BodyAccess::discard);
			}

			for (BlockPos trigger : compiled.triggers()) {
				this.wakeTriggered(trigger);
			}

			itr.remove();
		}

		if (newSectionBodies.isEmpty())
			return;

		try (BodyIdArray ids = new BodyIdArray(newSectionBodies.size())) {
			for (int i = 0; i < newSectionBodies.size(); i++) {
				ids.set(i, newSectionBodies.get(i).id());
			}

			long handle = this.bodies.addBodiesPrepare(ids, newSectionBodies.size());
			this.bodies.addBodiesFinalize(ids, newSectionBodies.size(), handle, EActivation.Activate);
		}
	}

	public void chunkLoaded(LevelChunk chunk) {
		LevelChunkSection[] sections = chunk.getSections();
		for (int i = 0; i < sections.length; i++) {
			LevelChunkSection section = sections[i];
			long pos = sectionPos(chunk, i);
			this.tryCompile(section, pos, null);
		}
	}

	public void chunkUnloaded(LevelChunk chunk) {
		LevelChunkSection[] sections = chunk.getSections();
		for (int i = 0; i < sections.length; i++) {
			long pos = sectionPos(chunk, i);

			CompileTask task = this.compilingSections.remove(pos);
			if (task != null) {
				task.discardResult();
			}

			List<BodyAccess> bodies = this.chunkSectionBodies.remove(pos);
			if (bodies != null) {
				bodies.forEach(BodyAccess::discard);
			}
		}
	}

	public void blockShapeChanged(BlockPos pos) {
		long sectionPos = SectionPos.blockToSection(pos.asLong());

		// don't remove the current bodies yet. they'll get replaced when the compile finishes.
		// no need to do anything here if there's no collision for this position
		if (!this.chunkSectionBodies.containsKey(sectionPos) && !this.compilingSections.containsKey(sectionPos))
			return;

		ChunkAccess chunk = this.level.getChunk(pos);
		int index = chunk.getSectionIndexFromSectionY(SectionPos.y(sectionPos));
		LevelChunkSection section = chunk.getSection(index);

		this.tryCompile(section, sectionPos, pos);
	}

	private void tryCompile(LevelChunkSection section, long pos, @Nullable BlockPos changed) {
		// remove here so even if there's no new compilation, the potential old one is discarded
		CompileTask replaced = this.compilingSections.remove(pos);

		if (replaced != null) {
			replaced.discardResult();
		}

		SectionShapeCompiler compiler = SectionShapeCompiler.create(this.boxCache, section, replaced, changed);

		if (compiler != null) {
			CompletableFuture<CompiledSection> future = CompletableFuture.supplyAsync(compiler, this.executor);
			CompileTask task = new CompileTask(compiler, future);
			this.compilingSections.put(pos, task);
		}
	}

	private void wakeWithin(ConstAaBox box) {
		this.bodies.activateBodiesInAaBox(box, DUMMY_BROAD_PHASE_FILTER, DUMMY_OBJECT_LAYER_FILTER);
	}

	private void wakeTriggered(BlockPos trigger) {
		try (AaBox box = new AaBox(
				new RVec3(trigger.getX() - 1, trigger.getY() - 1, trigger.getZ() - 1),
				new RVec3(trigger.getX() + 1, trigger.getY() + 1, trigger.getZ() + 1)
		)) {
			this.wakeWithin(box);
		}
	}

	public static VoxelShape getPhysicsVisibleShape(BlockState state) {
		return state.getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
	}

	private static long sectionPos(LevelChunk chunk, int sectionIndex) {
		return SectionPos.asLong(chunk.getPos().x, chunk.getSectionYFromSectionIndex(sectionIndex), chunk.getPos().z);
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
