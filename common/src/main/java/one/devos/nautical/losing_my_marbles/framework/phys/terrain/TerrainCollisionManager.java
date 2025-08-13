package one.devos.nautical.losing_my_marbles.framework.phys.terrain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.jetbrains.annotations.Nullable;

import com.github.stephengold.joltjni.AaBox;
import com.github.stephengold.joltjni.Body;
import com.github.stephengold.joltjni.BodyCreationSettings;
import com.github.stephengold.joltjni.BodyIdArray;
import com.github.stephengold.joltjni.RVec3;
import com.github.stephengold.joltjni.enumerate.EActivation;
import com.github.stephengold.joltjni.enumerate.EMotionType;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import one.devos.nautical.losing_my_marbles.framework.phys.BodyAccess;
import one.devos.nautical.losing_my_marbles.framework.phys.PhysicsEnvironment;
import one.devos.nautical.losing_my_marbles.framework.phys.core.ObjectLayers;
import one.devos.nautical.losing_my_marbles.framework.phys.terrain.compile.CompileTask;
import one.devos.nautical.losing_my_marbles.framework.phys.terrain.compile.CompiledSection;
import one.devos.nautical.losing_my_marbles.framework.phys.terrain.compile.SectionShapeCompiler;
import one.devos.nautical.losing_my_marbles.framework.phys.util.BoxCache;

public final class TerrainCollisionManager {
	private final PhysicsEnvironment environment;
	private final Executor executor;
	private final BoxCache boxCache;
	private final Long2ObjectMap<List<BodyAccess>> chunkSectionBodies;
	private final Long2ObjectMap<CompileTask> compilingSections;

	public TerrainCollisionManager(PhysicsEnvironment environment) {
		this.environment = environment;

		ResourceLocation dim = environment.level.dimension().location();
		this.executor = Util.backgroundExecutor().forName("TerrainCollisionManager/" + dim);

		this.boxCache = new BoxCache();
		this.chunkSectionBodies = new Long2ObjectOpenHashMap<>();
		this.compilingSections = new Long2ObjectOpenHashMap<>();
	}

	public void close() {
		this.chunkSectionBodies.values().stream()
				.flatMap(Collection::stream)
				.forEach(BodyAccess::discard);

		this.compilingSections.values().forEach(CompileTask::discardResult);
		this.boxCache.close();
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

		ChunkAccess chunk = this.environment.level.getChunk(pos);
		int index = chunk.getSectionIndexFromSectionY(SectionPos.y(sectionPos));
		LevelChunkSection section = chunk.getSection(index);

		this.tryCompile(section, sectionPos, pos);
	}

	public void tick() {
		// poll for any newly compiled sections
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

			long handle = this.environment.bodies.addBodiesPrepare(ids, newSectionBodies.size());
			this.environment.bodies.addBodiesFinalize(ids, newSectionBodies.size(), handle, EActivation.Activate);
		}
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

	private static AaBox boxAround(BlockPos pos) {
		return new AaBox(
				new RVec3(pos.getX() - 1, pos.getY() - 1, pos.getZ() - 1),
				new RVec3(pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1)
		);
	}

	private static long sectionPos(LevelChunk chunk, int sectionIndex) {
		return SectionPos.asLong(chunk.getPos().x, chunk.getSectionYFromSectionIndex(sectionIndex), chunk.getPos().z);
	}
}
