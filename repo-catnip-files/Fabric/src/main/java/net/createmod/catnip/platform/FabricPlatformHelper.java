package net.createmod.catnip.platform;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import io.github.fabricators_of_create.porting_lib.util.EnvExecutor;
import net.createmod.catnip.platform.services.PlatformHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

public class FabricPlatformHelper implements PlatformHelper {
	@Override
	public Loader getPlatform() {
		return Loader.FABRIC;
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
	public List<String> getLoadedMods() {
		List<String> modIds = new ArrayList<>();
		for (ModContainer mod : FabricLoader.getInstance().getAllMods())
			modIds.add(mod.getMetadata().getId());
		return modIds;
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
