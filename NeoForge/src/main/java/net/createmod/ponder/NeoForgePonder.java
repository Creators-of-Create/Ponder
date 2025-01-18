package net.createmod.ponder;

import java.util.Map;
import java.util.Set;

import net.createmod.catnip.command.CatnipCommands;
import net.createmod.catnip.config.ConfigBase;
import net.createmod.catnip.net.ConfigPathArgument;
import net.createmod.ponder.command.PonderCommands;
import net.createmod.ponder.enums.PonderConfig;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(Ponder.MOD_ID)
public class NeoForgePonder {

	private static final DeferredRegister<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPES = DeferredRegister.create(BuiltInRegistries.COMMAND_ARGUMENT_TYPE, Ponder.MOD_ID);

	private static final DeferredHolder<ArgumentTypeInfo<?, ?>, SingletonArgumentInfo<ConfigPathArgument>> CONFIG_PATH_ARGUMENT_TYPE = COMMAND_ARGUMENT_TYPES.register("config_path", () ->
		ArgumentTypeInfos.registerByClass(ConfigPathArgument.class, SingletonArgumentInfo.contextFree(ConfigPathArgument::new)));

	public NeoForgePonder(IEventBus modEventBus, ModContainer modContainer) {
		modEventBus.addListener(NeoForgePonder::init);

		COMMAND_ARGUMENT_TYPES.register(modEventBus);

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
			CatnipCommands.register(event.getDispatcher());
		}
	}

	@EventBusSubscriber(bus = Bus.MOD)
	public static class ModBusEvents {
		@SubscribeEvent
		public static void onLoad(ModConfigEvent.Loading event) {
			PonderConfig.onLoad(event.getConfig());
		}

		@SubscribeEvent
		public static void onReload(ModConfigEvent.Reloading event) {
			PonderConfig.onReload(event.getConfig());
		}
	}
}
