package net.createmod.catnip.platform;

import java.util.function.Supplier;
import java.util.stream.Stream;

import io.github.fabricators_of_create.porting_lib.util.EnvExecutor;
import net.createmod.catnip.platform.services.PlatformHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

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

	@Override
	public void executeOnClientOnly(Supplier<Runnable> toRun) {
		EnvExecutor.runWhenOn(EnvType.CLIENT, toRun);
	}

	@Override
	public void executeOnServerOnly(Supplier<Runnable> toRun) {
		EnvExecutor.runWhenOn(EnvType.SERVER, toRun);
	}
}
