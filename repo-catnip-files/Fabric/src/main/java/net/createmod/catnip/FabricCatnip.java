package net.createmod.catnip;

import net.createmod.catnip.command.CatnipCommands;
import net.createmod.catnip.config.ConfigBase;
import net.createmod.catnip.enums.CatnipConfig;
import net.createmod.catnip.net.ConfigPathArgument;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.commands.synchronization.ArgumentTypes;
import net.minecraft.commands.synchronization.EmptyArgumentSerializer;
import net.minecraftforge.api.ModLoadingContext;
import net.minecraftforge.api.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.config.ModConfig;

import java.util.Map;
import java.util.Set;

public class FabricCatnip implements ModInitializer {
	@Override
	public void onInitialize() {
		Catnip.init();
		ArgumentTypes.register(Catnip.asResource("config_path").toString(), ConfigPathArgument.class, new EmptyArgumentSerializer<>(ConfigPathArgument::path));
		registerConfigs();

		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> CatnipCommands.register(dispatcher));
		ModConfigEvent.LOADING.register(CatnipConfig::onLoad);
		ModConfigEvent.RELOADING.register(CatnipConfig::onReload);
	}

	private static void registerConfigs() {
		Set<Map.Entry<ModConfig.Type, ConfigBase>> entries = CatnipConfig.registerConfigs();
		for (Map.Entry<ModConfig.Type, ConfigBase> entry : entries) {
			ModLoadingContext.registerConfig(Catnip.MOD_ID, entry.getKey(), entry.getValue().specification);
		}
	}
}
