package net.createmod.catnip.api.config;

import net.createmod.catnip.api.config.definition.CatnipConfig;
import net.createmod.catnip.api.config.definition.CatnipConfig.Root;
import net.createmod.catnip.api.platform.ServiceHelper;

import java.util.Optional;

public interface CatnipConfigRegistry {
	CatnipConfigRegistry INSTANCE = ServiceHelper.load(CatnipConfigRegistry.class);

	/// Register a [CatnipConfig].
	/// @param id a unique identifier for the config
	/// @throws IllegalArgumentException if a config is already registered with the given ID
	void register(ConfigId id, CatnipConfig.Root config);

	/// Get the [CatnipConfig] with the given ID, if present.
	Optional<Root> get(ConfigId id);
}
