package one.devos.nautical.losing_my_marbles.framework.platform;

import java.util.ServiceLoader;

public class Services {
	public static <T> T load(Class<T> clazz) {
		return ServiceLoader.load(clazz)
				.findFirst()
				.orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
	}
}
