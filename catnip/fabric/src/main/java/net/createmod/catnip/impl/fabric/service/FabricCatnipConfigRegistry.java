package net.createmod.catnip.impl.fabric.service;

import java.util.Optional;

import net.createmod.catnip.api.config.CatnipConfigRegistry;
import net.createmod.catnip.api.config.ConfigId;
import net.createmod.catnip.api.config.definition.CatnipConfig;

public final class FabricCatnipConfigRegistry implements CatnipConfigRegistry {
	@Override
	public void register(ConfigId id, CatnipConfig.Root config) {
		throw new RuntimeException("NYI");
	}

	@Override
	public Optional<CatnipConfig.Root> get(ConfigId id) {
		return Optional.empty();
	}
}
