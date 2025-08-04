package one.devos.nautical.losing_my_marbles.framework.platform;

public interface PlatformHelper {
	PlatformHelper INSTANCE = Services.load(PlatformHelper.class);

	Env getEnvironment();
}
