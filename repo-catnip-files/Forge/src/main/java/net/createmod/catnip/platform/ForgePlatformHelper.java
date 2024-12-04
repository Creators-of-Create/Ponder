package net.createmod.catnip.platform;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import net.createmod.catnip.platform.services.PlatformHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.forgespi.language.IModInfo;

public class ForgePlatformHelper implements PlatformHelper {

	@Override
	public Loader getPlatform() {
		return Loader.FORGE;
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
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, toRun);
	}

	@Override
	public void executeOnServerOnly(Supplier<Runnable> toRun) {
		DistExecutor.unsafeRunWhenOn(Dist.DEDICATED_SERVER, toRun);
	}
}
