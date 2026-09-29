package net.createmod.catnip.impl.config;

import net.createmod.catnip.api.Catnip;
import net.createmod.catnip.api.config.CatnipConfigRegistry;
import net.createmod.catnip.api.config.ConfigId;
import net.createmod.catnip.api.config.ConfigSide;
import net.createmod.catnip.api.config.definition.CatnipConfig;
import net.createmod.catnip.api.config.definition.ConfigValue;

public final class CatnipModConfig extends CatnipConfig.Root {
	public static final CatnipModConfig INSTANCE = new CatnipModConfig();

	public final ConfigValue<PlacementIndicator> placementIndicator = e(PlacementIndicator.TEXTURE, "indicatorType", """
			What indicator should be used when showing where the assisted placement ends up relative to your crosshair.
			Choose 'NONE' to disable the Indicator altogether.
			""");

	public final ConfigValue<Float> indicatorScale = f(1.0f, 0f, "indicatorScale", """
			Change the size of the Indicator by this multiplier.
			""");

	private CatnipModConfig() {}

	public static void register() {
		CatnipConfigRegistry.INSTANCE.register(new ConfigId(Catnip.ID, ConfigSide.CLIENT), INSTANCE);
	}

	public enum PlacementIndicator {
		TEXTURE, TRIANGLE, NONE
	}
}
