package one.devos.nautical.losing_my_marbles.framework.phys.core;

import com.github.stephengold.joltjni.BroadPhaseLayerInterface;
import com.github.stephengold.joltjni.BroadPhaseLayerInterfaceTable;
import com.github.stephengold.joltjni.ObjectLayerPairFilter;
import com.github.stephengold.joltjni.ObjectLayerPairFilterTable;

import com.github.stephengold.joltjni.ObjectVsBroadPhaseLayerFilterTable;
import com.github.stephengold.joltjni.readonly.ConstObjectVsBroadPhaseLayerFilter;

import net.minecraft.Util;

/**
 * Each physics object is a member of one layer, which defines which other layers it can and cannot collide with.
 */
public final class ObjectLayers {
	public static final int MOVING = 0;
	public static final int STATIC = 1;

	public static final int OBJECT_LAYERS = 2;
	public static final int BROAD_PHASE_LAYERS = 1;

	public static final ObjectLayerPairFilter FILTER = Util.make(() -> {
		ObjectLayerPairFilterTable filter = new ObjectLayerPairFilterTable(OBJECT_LAYERS);
		filter.enableCollision(MOVING, MOVING);
		filter.enableCollision(MOVING, STATIC);
		filter.disableCollision(STATIC, STATIC);
		return filter;
	});

	public static final BroadPhaseLayerInterface BROAD_PHASE_MAPPING = Util.make(() -> {
		BroadPhaseLayerInterfaceTable map = new BroadPhaseLayerInterfaceTable(OBJECT_LAYERS, BROAD_PHASE_LAYERS);
		map.mapObjectToBroadPhaseLayer(MOVING, 0);
		map.mapObjectToBroadPhaseLayer(STATIC, 0);
		return map;
	});

	public static final ConstObjectVsBroadPhaseLayerFilter BROAD_PHASE_FILTER = new ObjectVsBroadPhaseLayerFilterTable(
			ObjectLayers.BROAD_PHASE_MAPPING, BROAD_PHASE_LAYERS, ObjectLayers.FILTER, OBJECT_LAYERS
	);
}
