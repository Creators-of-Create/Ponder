package net.createmod.ponder.impl.client;

import net.createmod.catnip.api.config.CatnipConfigRegistry;
import net.createmod.catnip.api.config.ConfigId;
import net.createmod.catnip.api.config.ConfigSide;
import net.createmod.catnip.api.config.definition.ConfigValue;
import net.createmod.catnip.api.config.definition.CatnipConfig;
import net.createmod.ponder.api.Ponder;

public final class PonderClientConfig extends CatnipConfig.Root {
	public static final PonderClientConfig INSTANCE = new PonderClientConfig();

	public final ConfigValue<Boolean> comfyReading = b(false, "comfyReading", """
		Slow down a ponder scene whenever there is text on screen.
		""");
	public final ConfigValue<Boolean> editingMode = b(false, "editingMode", """
		Show additional info in the ponder view and reload scene scripts more frequently.
		""");

	public static void register() {
		CatnipConfigRegistry.INSTANCE.register(new ConfigId(Ponder.MOD_ID, ConfigSide.CLIENT), INSTANCE);
	}
}
