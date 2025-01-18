package net.createmod.catnip.platform;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import net.createmod.catnip.platform.services.PlatformHelper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforgespi.language.IModInfo;

public class NeoForgePlatformHelper implements PlatformHelper {

	@Override
	public Loader getLoader() {
		return Loader.NEOFORGE;
	}

	@Override
	public Env getEnv() {
		return FMLLoader.getDist() == Dist.CLIENT ? Env.CLIENT : Env.SERVER;
	}

	@Override
	public boolean isModLoaded(String modId) {
		return ModList.get().isLoaded(modId);
	}

	@Override
	public boolean isDevelopmentEnvironment() {
		return !FMLLoader.isProduction();
	}

	@Override
	public List<String> getLoadedMods() {
		List<String> modIds = new ArrayList<>();
		for (IModInfo mod : ModList.get().getMods())
			modIds.add(mod.getModId());
		return modIds;
	}

	@Override
	public void executeOnClientOnly(Supplier<Runnable> toRun) {
		if (CatnipServices.PLATFORM.getEnv().isClient()) {
			toRun.get().run();
		}
	}

	@Override
	public void executeOnServerOnly(Supplier<Runnable> toRun) {
		if (CatnipServices.PLATFORM.getEnv().isServer()) {
			toRun.get().run();
		}
	}
}
