package net.createmod.catnip.platform;

import java.util.ServiceLoader;

import net.createmod.catnip.platform.services.ModFluidHelper;
import net.createmod.catnip.platform.services.ModHooksHelper;
import net.createmod.catnip.platform.services.NetworkHelper;
import net.createmod.catnip.platform.services.PlatformHelper;
import net.createmod.ponder.Ponder;

public class CatnipServices {

	public static final PlatformHelper PLATFORM = load(PlatformHelper.class);
	public static final ModFluidHelper<?> FLUID_HELPER = load(ModFluidHelper.class);
	public static final ModHooksHelper HOOKS = load(ModHooksHelper.class);
	public static final NetworkHelper NETWORK = load(NetworkHelper.class);

	public static <T> T load(Class<T> clazz) {
		final T loadedService = ServiceLoader.load(clazz)
				.findFirst()
				.orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
		Ponder.LOGGER.debug("Loaded {} for service {}", loadedService, clazz);
		return loadedService;
	}

}
