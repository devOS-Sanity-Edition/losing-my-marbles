package one.devos.nautical.losing_my_marbles.framework.phys.terrain;

import net.minecraft.core.BlockPos;

import java.util.List;
import java.util.Set;

/**
 * @param triggers set of positions that changed to trigger this compile
 */
public record CompiledSection(List<SectionShape> shapes, Set<BlockPos> triggers) {
}
