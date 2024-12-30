package net.createmod.ponder;

import java.util.Map;
import java.util.Set;

import net.createmod.catnip.config.ConfigBase;
import net.createmod.ponder.command.PonderCommands;
import net.createmod.ponder.enums.PonderConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@Mod(Ponder.MOD_ID)
public class NeoForgePonder {

	public NeoForgePonder(IEventBus modEventBus, ModContainer modContainer) {
		modEventBus.addListener(NeoForgePonder::init);

		registerConfigs(modContainer);
	}

	public static void init(final FMLCommonSetupEvent event) {
		Ponder.init();
	}

	private static void registerConfigs(ModContainer modContainer) {
		Set<Map.Entry<ModConfig.Type, ConfigBase>> entries = PonderConfig.registerConfigs();
		for (Map.Entry<ModConfig.Type, ConfigBase> entry : entries) {
			modContainer.registerConfig(entry.getKey(), entry.getValue().specification);
		}
	}

	@EventBusSubscriber
	public static class Events {
		@SubscribeEvent
		public static void registerCommands(RegisterCommandsEvent event) {
			PonderCommands.register(event.getDispatcher());
		}
	}
}
