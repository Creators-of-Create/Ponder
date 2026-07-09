package net.createmod.catnip.api.platform;

import net.createmod.catnip.api.network.NetworkHelper;
import net.createmod.catnip.api.platform.services.PlatformHelper;

public final class CatnipServices {
	public static final PlatformHelper PLATFORM = PlatformHelper.INSTANCE;
	public static final NetworkHelper NETWORK = NetworkHelper.INSTANCE;

	private CatnipServices() {
	}
}
