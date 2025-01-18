package net.createmod.ponder;

import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry;
import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeModConfigEvents;
import net.createmod.catnip.command.CatnipCommands;
import net.createmod.catnip.config.ConfigBase;
import net.createmod.catnip.net.ConfigPathArgument;
import net.createmod.ponder.command.PonderCommands;
import net.createmod.ponder.enums.PonderConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.server.MinecraftServer;
import net.neoforged.fml.config.ModConfig;

import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public class FabricPonder implements ModInitializer {
	@Nullable
	private static MinecraftServer server = null;

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

		NeoForgeModConfigEvents.loading(Ponder.MOD_ID).register(PonderConfig::onLoad);
		NeoForgeModConfigEvents.reloading(Ponder.MOD_ID).register(PonderConfig::onReload);

		ServerLifecycleEvents.SERVER_STARTED.register(s -> server = s);
		ServerLifecycleEvents.SERVER_STOPPED.register(s -> server = null);
	}

	private static void registerConfigs() {
		Set<Map.Entry<ModConfig.Type, ConfigBase>> entries = PonderConfig.registerConfigs();
		for (Map.Entry<ModConfig.Type, ConfigBase> entry : entries) {
			NeoForgeConfigRegistry.INSTANCE.register(Ponder.MOD_ID, entry.getKey(), entry.getValue().specification);
		}
	}

	// TODO - Move maybe?
	@Nullable
	public static MinecraftServer getServer() {
		return server;
	}
}
