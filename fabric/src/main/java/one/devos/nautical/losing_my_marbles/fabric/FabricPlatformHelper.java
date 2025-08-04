package one.devos.nautical.losing_my_marbles.fabric;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import one.devos.nautical.losing_my_marbles.framework.platform.Env;
import one.devos.nautical.losing_my_marbles.framework.platform.PlatformHelper;

public class FabricPlatformHelper implements PlatformHelper {
	@Override
	public Env getEnvironment() {
		return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT ? Env.CLIENT : Env.SERVER;
	}
}
