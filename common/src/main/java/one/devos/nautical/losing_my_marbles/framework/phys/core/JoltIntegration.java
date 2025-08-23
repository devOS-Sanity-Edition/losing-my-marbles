package one.devos.nautical.losing_my_marbles.framework.phys.core;

import com.github.stephengold.joltjni.JobSystem;
import com.github.stephengold.joltjni.JobSystemThreadPool;
import com.github.stephengold.joltjni.Jolt;
import com.github.stephengold.joltjni.PhysicsSystem;
import com.github.stephengold.joltjni.RVec3;
import com.github.stephengold.joltjni.readonly.RVec3Arg;
import com.github.stephengold.joltjni.readonly.Vec3Arg;

import net.minecraft.world.phys.Vec3;

public final class JoltIntegration {
	public static final int MAX_BODIES = 65536; // 2^16
	public static final int MAX_PAIRS = 4096;
	public static final int MAX_CONTACTS = 16384; // 2^14

	public static void setup() {
		JoltNatives.load();
		Jolt.registerDefaultAllocator();
		Jolt.installDefaultAssertCallback();
		Jolt.installDefaultTraceCallback();

		if (!Jolt.newFactory()) {
			throw new RuntimeException("Failed to create Jolt factory");
		}

		Jolt.registerTypes();
	}

	public static PhysicsSystem createSystem() {
		PhysicsSystem system = new PhysicsSystem();

		system.init(
				MAX_BODIES, 0, MAX_PAIRS, MAX_CONTACTS,
				ObjectLayers.BROAD_PHASE_MAPPING, ObjectLayers.BROAD_PHASE_FILTER, ObjectLayers.FILTER
		);

		return system;
	}

	public static JobSystem createJobSystem() {
		// number of threads to use will be automatically determined
		return new JobSystemThreadPool(Jolt.cMaxPhysicsJobs, Jolt.cMaxPhysicsBarriers);
	}

	public static RVec3 convert(Vec3 vec) {
		return new RVec3(vec.x, vec.y, vec.z);
	}

	public static com.github.stephengold.joltjni.Vec3 convertF(Vec3 vec) {
		return new com.github.stephengold.joltjni.Vec3(vec.x, vec.y, vec.z);
	}

	public static Vec3 convert(RVec3Arg vec) {
		return new Vec3(vec.xx(), vec.yy(),vec.zz());
	}

	public static Vec3 convert(Vec3Arg vec) {
		return new Vec3(vec.getX(), vec.getY(),vec.getZ());
	}
}
