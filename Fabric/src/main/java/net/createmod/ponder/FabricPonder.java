package net.createmod.ponder;

import java.util.Map;
import java.util.Set;

import net.createmod.catnip.config.ConfigBase;
import net.createmod.ponder.command.PonderCommands;
import net.createmod.ponder.enums.PonderConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraftforge.api.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

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
			ModLoadingContext.registerConfig(Ponder.MOD_ID, entry.getKey(), entry.getValue().specification);
		}
	}
}
