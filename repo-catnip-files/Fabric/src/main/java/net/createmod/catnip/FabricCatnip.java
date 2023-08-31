package net.createmod.catnip;

import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import fuzs.forgeconfigapiport.api.config.v2.ModConfigEvents;
import net.createmod.catnip.command.CatnipCommands;
import net.createmod.catnip.config.ConfigBase;
import net.createmod.catnip.enums.CatnipConfig;
import net.createmod.catnip.net.ConfigPathArgument;
import net.createmod.catnip.net.FabricCatnipNetwork;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraftforge.fml.config.ModConfig;

import java.util.Map;
import java.util.Set;

public class FabricCatnip implements ModInitializer {
	@Override
	public void onInitialize() {
		Catnip.init();
		ArgumentTypeRegistry.registerArgumentType(Catnip.asResource("config_path"), ConfigPathArgument.class,
												  SingletonArgumentInfo.contextFree(ConfigPathArgument::new));
		registerConfigs();
		FabricCatnipNetwork.register();

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> CatnipCommands.register(dispatcher));
		ModConfigEvents.loading(Catnip.MOD_ID).register(CatnipConfig::onLoad);
		ModConfigEvents.reloading(Catnip.MOD_ID).register(CatnipConfig::onReload);
	}

	private static void registerConfigs() {
		Set<Map.Entry<ModConfig.Type, ConfigBase>> entries = CatnipConfig.registerConfigs();
		for (Map.Entry<ModConfig.Type, ConfigBase> entry : entries) {
			ForgeConfigRegistry.INSTANCE.register(Catnip.MOD_ID, entry.getKey(), entry.getValue().specification);
		}
	}
}
