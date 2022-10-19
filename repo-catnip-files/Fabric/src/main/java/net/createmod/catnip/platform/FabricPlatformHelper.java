package net.createmod.catnip.platform;

import net.createmod.catnip.platform.services.PlatformHelper;
import net.fabricmc.loader.api.FabricLoader;

import java.util.stream.Stream;

public class FabricPlatformHelper implements PlatformHelper {
	@Override
	public String getPlatformName() {
		return "Fabric";
	}

	@Override
	public boolean isModLoaded(String modId) {
		return FabricLoader.getInstance().isModLoaded(modId);
	}

	@Override
	public boolean isDevelopmentEnvironment() {
		return FabricLoader.getInstance().isDevelopmentEnvironment();
	}

	@Override
	public Stream<String> getLoadedMods() {
		return FabricLoader.getInstance().getAllMods().stream().map(modContainer -> modContainer.getMetadata().getId());
	}
}
