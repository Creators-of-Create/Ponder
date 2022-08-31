package net.createmod.catnip.platform;

import java.util.ServiceLoader;

import net.createmod.catnip.Catnip;
import net.createmod.catnip.platform.services.ModHooksHelper;
import net.createmod.catnip.platform.services.NetworkHelper;
import net.createmod.catnip.platform.services.PlatformHelper;
import net.createmod.catnip.platform.services.RegisteredObjectsHelper;

public class CatnipServices {

	public static final PlatformHelper PLATFORM = load(PlatformHelper.class);
	public static final ModHooksHelper HOOKS = load(ModHooksHelper.class);
	public static final RegisteredObjectsHelper REGISTRIES = load(RegisteredObjectsHelper.class);
	public static final NetworkHelper NETWORK = load(NetworkHelper.class);

	public static <T> T load(Class<T> clazz) {
		final T loadedService = ServiceLoader.load(clazz)
				.findFirst()
				.orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
		Catnip.LOGGER.debug("Loaded {} for service {}", loadedService, clazz);
		return loadedService;
	}

}
