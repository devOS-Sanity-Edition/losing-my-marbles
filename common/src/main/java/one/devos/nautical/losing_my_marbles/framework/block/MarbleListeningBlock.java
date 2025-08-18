package one.devos.nautical.losing_my_marbles.framework.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import one.devos.nautical.losing_my_marbles.content.marble.MarbleEntity;

public interface MarbleListeningBlock {
	void marbleEntered(ServerLevel level, BlockState state, BlockPos pos, MarbleEntity entity);
}
