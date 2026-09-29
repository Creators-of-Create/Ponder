package net.createmod.catnip.impl.fabric.service;

import java.util.LinkedHashSet;
import java.util.SequencedSet;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import net.createmod.catnip.impl.config.ConfigHelper;
import net.createmod.catnip.api.platform.Env;
import net.createmod.catnip.api.platform.Loader;
import net.createmod.catnip.api.platform.services.PlatformHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

public class FabricPlatformHelper implements PlatformHelper {
	@Override
	public Loader getLoader() {
		return Loader.FABRIC;
	}

	@Override
	public Env getEnv() {
		return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT ? Env.CLIENT : Env.SERVER;
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
	public SequencedSet<String> getLoadedModIds() {
		return FabricLoader.getInstance().getAllMods().stream()
			.map(mod -> mod.getMetadata().getId())
			.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	@Override
	public String getModDisplayName(String modId) {
		return FabricLoader.getInstance().getModContainer(modId)
				.map(mod -> mod.getMetadata().getName())
				.orElse(ConfigHelper.toHumanReadable(modId));
	}

	@Override
	public void executeOnClientOnly(Supplier<Runnable> toRun) {
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT)
			toRun.get().run();
	}

	@Override
	public void executeOnServerOnly(Supplier<Runnable> toRun) {
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER)
			toRun.get().run();
	}
}
