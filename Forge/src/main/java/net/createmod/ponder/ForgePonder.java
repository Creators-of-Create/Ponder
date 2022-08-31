package net.createmod.ponder;

import java.util.Map;
import java.util.Set;

import net.createmod.catnip.config.ConfigBase;
import net.createmod.ponder.command.PonderCommands;
import net.createmod.ponder.enums.PonderConfig;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Ponder.MOD_ID)
public class ForgePonder {

	public ForgePonder() {
		ModLoadingContext modLoadingContext = ModLoadingContext.get();
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;

		modEventBus.addListener(ForgePonder::init);

		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ForgePonderClient.onCtor(modEventBus, forgeEventBus));

		registerConfigs(modLoadingContext);
	}

	public static void init(final FMLCommonSetupEvent event) {
		Ponder.init();
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
		}

	}

}
