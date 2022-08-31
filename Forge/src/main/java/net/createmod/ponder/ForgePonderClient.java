package net.createmod.ponder;

import java.util.Optional;

import net.createmod.catnip.config.ui.BaseConfigScreen;
import net.createmod.catnip.utility.Couple;
import net.createmod.catnip.utility.theme.Color;
import net.createmod.ponder.enums.PonderConfig;
import net.createmod.ponder.foundation.PonderTooltipHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;

public class ForgePonderClient {

	public static void onCtor(IEventBus modEventBus, IEventBus forgeEventBus) {
		modEventBus.addListener(ForgePonderClient::init);
	}

	public static void init(final FMLClientSetupEvent event) {
		PonderClient.init();
	}

	@Mod.EventBusSubscriber(Dist.CLIENT)
	public static class ClientEvents {

		@SubscribeEvent
		public static void onTick(TickEvent.ClientTickEvent event) {
			PonderTooltipHandler.tick();
		}


		@SubscribeEvent
		public static void onRenderTooltipColor(RenderTooltipEvent.Color event) {
			Optional<Couple<Color>> colors = PonderTooltipHandler.handleTooltipColor(event.getItemStack());
			if (colors.isEmpty())
				return;

			event.setBorderStart(colors.get().getFirst().getRGB());
			event.setBorderEnd(colors.get().getSecond().getRGB());
		}

		@SubscribeEvent
		public static void onItemTooltip(ItemTooltipEvent event) {
			PonderTooltipHandler.addToTooltip(event.getToolTip(), event.getItemStack());
		}

	}

	@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
	public static class ModBusClientEvents {
		@SubscribeEvent
		public static void loadCompleted(FMLLoadCompleteEvent event) {
			PonderClient.modLoadCompleted();

			ModContainer modContainer = ModList.get()
					.getModContainerById(Ponder.MOD_ID)
					.orElseThrow(() -> new IllegalStateException("Ponder Mod Container missing after loadCompleted"));
			modContainer.registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class,
					() -> new ConfigGuiHandler.ConfigGuiFactory(
							(mc, previousScreen) -> new BaseConfigScreen(previousScreen, Ponder.MOD_ID)));

			BaseConfigScreen.setDefaultActionFor(Ponder.MOD_ID, base -> base
					.withTitles("Client Settings", null, null)
					.withSpecs(PonderConfig.Client().specification, null, null)
			);
		}
	}

}
