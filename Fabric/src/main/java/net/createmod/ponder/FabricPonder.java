package net.createmod.ponder;

import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry;
import net.createmod.catnip.config.ConfigBase;
import net.createmod.ponder.command.PonderCommands;
import net.createmod.ponder.enums.PonderConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.neoforged.fml.config.ModConfig;

import java.util.Map;
import java.util.Set;

public class FabricPonder implements ModInitializer {


	@Override
	public void onInitialize() {
		Ponder.init();

		registerConfigs();

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> PonderCommands.register(dispatcher));
	}

	private static void registerConfigs() {
		Set<Map.Entry<ModConfig.Type, ConfigBase>> entries = PonderConfig.registerConfigs();
		for (Map.Entry<ModConfig.Type, ConfigBase> entry : entries) {
			NeoForgeConfigRegistry.INSTANCE.register(Ponder.MOD_ID, entry.getKey(), entry.getValue().specification);
		}
	}
}
