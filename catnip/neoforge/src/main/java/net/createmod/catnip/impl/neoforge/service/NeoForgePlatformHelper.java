package net.createmod.catnip.impl.neoforge.service;

import java.util.LinkedHashSet;
import java.util.SequencedSet;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import net.createmod.catnip.impl.config.ConfigHelper;
import net.createmod.catnip.api.platform.Env;
import net.createmod.catnip.api.platform.Loader;
import net.createmod.catnip.api.platform.services.PlatformHelper;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforgespi.language.IModInfo;

public class NeoForgePlatformHelper implements PlatformHelper {
	@Override
	public Loader getLoader() {
		return Loader.NEOFORGE;
	}

	@Override
	public Env getEnv() {
		return FMLEnvironment.getDist() == Dist.CLIENT ? Env.CLIENT : Env.SERVER;
	}

	@Override
	public boolean isModLoaded(String modId) {
		return ModList.get().isLoaded(modId);
	}

	@Override
	public boolean isDevelopmentEnvironment() {
		return !FMLEnvironment.isProduction();
	}

	@Override
	public SequencedSet<String> getLoadedModIds() {
		return ModList.get().getMods().stream()
			.map(IModInfo::getModId)
			.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	@Override
	public String getModDisplayName(String modId) {
		return ModList.get().getModContainerById(modId)
				.map(mod -> mod.getModInfo().getDisplayName())
				.orElse(ConfigHelper.toHumanReadable(modId));
	}

	@Override
	public void executeOnClientOnly(Supplier<Runnable> toRun) {
		if (PlatformHelper.INSTANCE.getEnv().isClient()) {
			toRun.get().run();
		}
	}

	@Override
	public void executeOnServerOnly(Supplier<Runnable> toRun) {
		if (PlatformHelper.INSTANCE.getEnv().isServer()) {
			toRun.get().run();
		}
	}
}
