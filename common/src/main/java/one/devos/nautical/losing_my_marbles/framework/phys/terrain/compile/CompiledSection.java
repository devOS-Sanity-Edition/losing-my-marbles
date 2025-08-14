package one.devos.nautical.losing_my_marbles.framework.phys.terrain.compile;

import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;

/**
 * @param triggers set of positions that changed to trigger this compile
 */
public record CompiledSection(List<SectionShape> shapes, Set<BlockPos> triggers) {
}
