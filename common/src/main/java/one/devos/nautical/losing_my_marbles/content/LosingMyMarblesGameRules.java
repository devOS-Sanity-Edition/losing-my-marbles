package one.devos.nautical.losing_my_marbles.content;

import net.minecraft.world.level.GameRules;
import one.devos.nautical.losing_my_marbles.framework.platform.PlatformHelper;

public final class LosingMyMarblesGameRules {
	public static final GameRules.Key<GameRules.IntegerValue> PHYSICS_STEPS_PER_TICK = registerInt(
			"physicsStepsPerTick", GameRules.Category.MISC, 5, 1
	);
	public static final GameRules.Key<GameRules.IntegerValue> MARBLE_DESPAWN_TIMER = registerInt(
			"marbleDespawnTimer", GameRules.Category.MISC, 0, 0 // default is no limit
	);
	public static final GameRules.Key<GameRules.IntegerValue> STATIONARY_MARBLE_DESPAWN_TIMER = registerInt(
			"stationaryMarbleDespawnTimer", GameRules.Category.MISC, 0, 0 // default is no limit
	);

	public static void init() {
	}

	private static GameRules.Key<GameRules.IntegerValue> registerInt(String name, GameRules.Category category, int defaultValue, int minValue) {
		GameRules.Type<GameRules.IntegerValue> type = PlatformHelper.INSTANCE.createIntegerRule(defaultValue, minValue);
		return PlatformHelper.INSTANCE.registerGameRule(name, category, type);
	}
}
