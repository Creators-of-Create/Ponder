package net.createmod.ponder;

import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import fuzs.forgeconfigapiport.api.config.v2.ModConfigEvents;
import net.createmod.catnip.command.CatnipCommands;
import net.createmod.catnip.config.ConfigBase;
import net.createmod.catnip.net.ConfigPathArgument;
import net.createmod.ponder.net.FabricPonderNetwork;
import net.createmod.ponder.command.PonderCommands;
import net.createmod.ponder.enums.PonderConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraftforge.fml.config.ModConfig;

import java.util.Map;
import java.util.Set;

public class FabricPonder implements ModInitializer {


	@Override
	public void onInitialize() {
		Ponder.init();

		registerConfigs();

		ArgumentTypeRegistry.registerArgumentType(Ponder.asResource("config_path"), ConfigPathArgument.class,
			SingletonArgumentInfo.contextFree(ConfigPathArgument::new));

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			PonderCommands.register(dispatcher);
			CatnipCommands.register(dispatcher);
		});

		FabricPonderNetwork.register();

		ModConfigEvents.loading(Ponder.MOD_ID).register(PonderConfig::onLoad);
		ModConfigEvents.reloading(Ponder.MOD_ID).register(PonderConfig::onReload);
	}

	private static void registerConfigs() {
		Set<Map.Entry<ModConfig.Type, ConfigBase>> entries = PonderConfig.registerConfigs();
		for (Map.Entry<ModConfig.Type, ConfigBase> entry : entries) {
			ForgeConfigRegistry.INSTANCE.register(Ponder.MOD_ID, entry.getKey(), entry.getValue().specification);
		}
	}
}
