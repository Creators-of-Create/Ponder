package net.createmod.ponder;

import java.util.Map;
import java.util.Set;

import net.createmod.catnip.command.CatnipCommands;
import net.createmod.catnip.config.ConfigBase;
import net.createmod.catnip.net.ConfigPathArgument;
import net.createmod.ponder.command.PonderCommands;
import net.createmod.ponder.enums.PonderConfig;
import net.createmod.ponder.net.ForgePonderNetwork;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(Ponder.MOD_ID)
public class ForgePonder {

	private static final DeferredRegister<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPES = DeferredRegister.create(ForgeRegistries.COMMAND_ARGUMENT_TYPES, Ponder.MOD_ID);

	private static final RegistryObject<SingletonArgumentInfo<ConfigPathArgument>> CONFIG_PATH_ARGUMENT_TYPE = COMMAND_ARGUMENT_TYPES.register("config_path", () ->
		ArgumentTypeInfos.registerByClass(ConfigPathArgument.class, SingletonArgumentInfo.contextFree(ConfigPathArgument::new)));

	public ForgePonder() {
		ModLoadingContext modLoadingContext = ModLoadingContext.get();
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;

		modEventBus.addListener(ForgePonder::init);

		COMMAND_ARGUMENT_TYPES.register(modEventBus);

		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ForgePonderClient.onCtor(modEventBus, forgeEventBus));

		registerConfigs(modLoadingContext);
	}

	public static void init(final FMLCommonSetupEvent event) {
		Ponder.init();

		ForgePonderNetwork.register();
	}

	private static void registerConfigs(ModLoadingContext modLoadingContext) {
		Set<Map.Entry<ModConfig.Type, ConfigBase>> entries = PonderConfig.registerConfigs();
		for (Map.Entry<ModConfig.Type, ConfigBase> entry : entries) {
			modLoadingContext.registerConfig(entry.getKey(), entry.getValue().specification);
		}
	}

	@Mod.EventBusSubscriber
	public static class Events {

		@SubscribeEvent
		public static void registerCommands(RegisterCommandsEvent event) {
			PonderCommands.register(event.getDispatcher());
			CatnipCommands.register(event.getDispatcher());
		}

	}

	@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
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
