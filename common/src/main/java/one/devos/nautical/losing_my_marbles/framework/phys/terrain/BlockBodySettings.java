package one.devos.nautical.losing_my_marbles.framework.phys.terrain;

import com.github.stephengold.joltjni.BodyCreationSettings;

import com.github.stephengold.joltjni.MassProperties;
import com.github.stephengold.joltjni.Vec3;
import com.github.stephengold.joltjni.enumerate.EMotionType;

import net.minecraft.world.level.block.state.BlockState;
import one.devos.nautical.losing_my_marbles.framework.phys.core.ObjectLayers;

public final class BlockBodySettings {
	public static void configure(BodyCreationSettings settings, BlockState state) {
		// general properties for all block bodies
		settings.setMotionType(EMotionType.Static);
		settings.setAllowSleeping(false);
		settings.setObjectLayer(ObjectLayers.STATIC);
		MassProperties mass = new MassProperties();
		mass.setMassAndInertiaOfSolidBox(new Vec3(1, 1, 1), 10);
		settings.setMassPropertiesOverride(mass);

		// state-specific properties

		// Minecraft friction is a percentage of velocity to maintain, from 0-1
		// Jolt friction ???
		settings.setFriction(state.getBlock().getFriction());

		// TODO
		settings.setRestitution(0.5f);
	}
}
