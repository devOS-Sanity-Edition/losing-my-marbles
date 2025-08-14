package one.devos.nautical.losing_my_marbles.framework.phys.terrain;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.jetbrains.annotations.Nullable;

import com.github.stephengold.joltjni.AaBox;
import com.github.stephengold.joltjni.Body;
import com.github.stephengold.joltjni.BodyCreationSettings;
import com.github.stephengold.joltjni.BodyIdArray;
import com.github.stephengold.joltjni.Jolt;
import com.github.stephengold.joltjni.RVec3;
import com.github.stephengold.joltjni.TransformedShape;
import com.github.stephengold.joltjni.enumerate.EActivation;
import com.github.stephengold.joltjni.enumerate.EMotionType;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongConsumer;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.AABB;
import one.devos.nautical.losing_my_marbles.framework.phys.BodyAccess;
import one.devos.nautical.losing_my_marbles.framework.phys.PhysicsEnvironment;
import one.devos.nautical.losing_my_marbles.framework.phys.core.ObjectLayers;
import one.devos.nautical.losing_my_marbles.framework.phys.debug.DebugGeometryOutput;
import one.devos.nautical.losing_my_marbles.framework.phys.terrain.compile.CompileTask;
import one.devos.nautical.losing_my_marbles.framework.phys.terrain.compile.CompiledSection;
import one.devos.nautical.losing_my_marbles.framework.phys.terrain.compile.SectionShapeCompiler;
import one.devos.nautical.losing_my_marbles.framework.phys.util.BoxCache;

public final class TerrainCollisionManager {
	private final PhysicsEnvironment environment;
	private final Executor executor;
	private final BoxCache boxCache;
	private final Long2ObjectMap<SectionEntry> sectionEntries;
	private final Long2ObjectMap<CompileTask> compilingSections;
	private final LongSet awaitingChunkSections;

	public TerrainCollisionManager(PhysicsEnvironment environment) {
		this.environment = environment;

		ResourceLocation dim = environment.level.dimension().location();
		this.executor = Util.backgroundExecutor().forName("TerrainCollisionManager/" + dim);

		this.boxCache = new BoxCache();
		this.sectionEntries = new Long2ObjectOpenHashMap<>();
		this.compilingSections = new Long2ObjectOpenHashMap<>();
		this.awaitingChunkSections = new LongOpenHashSet();
	}

	public void close() {
		this.sectionEntries.values().forEach(SectionEntry::discard);
		this.compilingSections.values().forEach(CompileTask::discardResult);
		this.boxCache.close();
	}

	public void chunkLoaded(LevelChunk chunk) {
		LevelChunkSection[] sections = chunk.getSections();
		for (int i = 0; i < sections.length; i++) {
			LevelChunkSection section = sections[i];
			long pos = sectionPos(chunk, i);

			if (this.awaitingChunkSections.remove(pos)) {
				this.tryCompile(section, pos, null);
			}
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

			SectionEntry entry = this.sectionEntries.remove(pos);
			if (entry != null) {
				entry.discard();
			}

			this.awaitingChunkSections.add(pos);
		}
	}

	public void blockShapeChanged(BlockPos pos) {
		long sectionPos = SectionPos.blockToSection(pos.asLong());

		// don't remove the current bodies yet. they'll get replaced when the compile finishes.
		// no need to do anything here if there's no collision for this position
		if (!this.sectionEntries.containsKey(sectionPos) && !this.compilingSections.containsKey(sectionPos))
			return;

		ChunkAccess chunk = this.environment.level.getChunk(pos);
		int index = chunk.getSectionIndexFromSectionY(SectionPos.y(sectionPos));
		LevelChunkSection section = chunk.getSection(index);

		this.tryCompile(section, sectionPos, pos);
	}

	public void prepareArea(AABB area) {
		forEachSectionInBox(area, section -> {
			SectionEntry entry = this.sectionEntries.get(section);
			if (entry != null) {
				entry.refresh();
				return;
			}

			if (this.compilingSections.containsKey(section) || this.awaitingChunkSections.contains(section))
				return;

			LevelChunkSection chunkSection = this.getSection(section);
			if (chunkSection == null) {
				this.awaitingChunkSections.add(section);
				return;
			}

			this.tryCompile(chunkSection, section, null);
		});
	}

	public void tick() {
		// remove all sections that are no longer in use
		this.sectionEntries.values().removeIf(entry -> {
			entry.tick();

			if (entry.shouldRemove()) {
				entry.discard();
				return true;
			}

			return false;
		});

		// poll for any newly finished compilations
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

					Body body = this.environment.bodies.createBody(settings);
					// this cast is stupid
					return (BodyAccess) new BodyAccess.Impl(body, this.environment.bodies);
				}
			}).toList();

			newSectionBodies.addAll(bodies);
			SectionEntry oldEntry = this.sectionEntries.put(pos, new SectionEntry(bodies));
			if (oldEntry != null) {
				oldEntry.discard();
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

			long handle = this.environment.bodies.addBodiesPrepare(ids, newSectionBodies.size());
			this.environment.bodies.addBodiesFinalize(ids, newSectionBodies.size(), handle, EActivation.Activate);
		}
	}

	public void collectDebugGeometry(AABB area, DebugGeometryOutput output) {
		forEachSectionInBox(area, pos -> {
			SectionEntry entry = this.sectionEntries.get(pos);
			if (entry == null)
				return;

			for (BodyAccess body : entry.bodies) {
				try (TransformedShape shape = body.getBody().getTransformedShape()) {
					int triangles = shape.countDebugTriangles();
					FloatBuffer buffer = Jolt.newDirectFloatBuffer(triangles * 3 * 3);
					shape.copyDebugTriangles(buffer);
					output.accept(buffer);
				}
			}
		});
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

	private void wakeTriggered(BlockPos trigger) {
		try (AaBox box = boxAround(trigger)) {
			this.environment.wakeWithin(box);
		}
	}

	@Nullable
	private LevelChunkSection getSection(long pos) {
		int posX = SectionPos.x(pos);
		int posZ = SectionPos.z(pos);
		ChunkAccess chunk = this.environment.level.getChunk(posX, posZ, ChunkStatus.FULL, false);
		if (chunk == null)
			return null;

		int index = chunk.getSectionIndexFromSectionY(SectionPos.y(pos));
		return index < 0 ? null : chunk.getSection(index);
	}

	private static AaBox boxAround(BlockPos pos) {
		return new AaBox(
				new RVec3(pos.getX() - 1, pos.getY() - 1, pos.getZ() - 1),
				new RVec3(pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1)
		);
	}

	private static long sectionPos(LevelChunk chunk, int sectionIndex) {
		return SectionPos.asLong(chunk.getPos().x, chunk.getSectionYFromSectionIndex(sectionIndex), chunk.getPos().z);
	}

	private static void forEachSectionInBox(AABB box, LongConsumer consumer) {
		forEachSectionInBox(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, consumer);
	}

	private static void forEachSectionInBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, LongConsumer consumer) {
		int minSectionX = SectionPos.blockToSectionCoord(minX);
		int minSectionY = SectionPos.blockToSectionCoord(minY);
		int minSectionZ = SectionPos.blockToSectionCoord(minZ);
		int maxSectionX = SectionPos.blockToSectionCoord(maxX);
		int maxSectionY = SectionPos.blockToSectionCoord(maxY);
		int maxSectionZ = SectionPos.blockToSectionCoord(maxZ);

		for (int x = minSectionX; x <= maxSectionX; x++) {
			for (int y = minSectionY; y <= maxSectionY; y++) {
				for (int z = minSectionZ; z <= maxSectionZ; z++) {
					consumer.accept(SectionPos.asLong(x, y, z));
				}
			}
		}
	}

	private static final class SectionEntry {
		private static final int LIFE = 100;

		private final List<BodyAccess> bodies;
		private int life;


		private SectionEntry(List<BodyAccess> bodies) {
			this.bodies = bodies;
			this.refresh();
		}

		private void tick() {
			this.life--;
		}

		private void refresh() {
			this.life = LIFE;
		}

		private boolean shouldRemove() {
			return this.life <= 0;
		}

		private void discard() {
			this.bodies.forEach(BodyAccess::discard);
		}
	}
}
