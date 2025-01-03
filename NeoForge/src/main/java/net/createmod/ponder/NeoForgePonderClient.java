package net.createmod.ponder;

import java.util.Optional;
import java.util.function.Supplier;

import net.createmod.catnip.config.ui.BaseConfigScreen;
import net.createmod.catnip.utility.Couple;
import net.createmod.catnip.utility.theme.Color;
import net.createmod.ponder.enums.PonderConfig;
import net.createmod.ponder.enums.PonderKeybinds;
import net.createmod.ponder.foundation.PonderTooltipHandler;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@Mod(value = Ponder.MOD_ID, dist = Dist.CLIENT)
public class NeoForgePonderClient {
	public NeoForgePonderClient(IEventBus modEventBus) {
		modEventBus.addListener(NeoForgePonderClient::init);
	}

	public static void init(final FMLClientSetupEvent event) {
		PonderClient.init();
	}

	@EventBusSubscriber(Dist.CLIENT)
	public static class ClientEvents {
		// TODO - Check if this is correct, not sure if it should be pre or post
		@SubscribeEvent
		public static void onTickPre(ClientTickEvent.Pre event) {
			PonderTooltipHandler.tick();
		}

		@SubscribeEvent
		public static void onTickPost(ClientTickEvent.Post event) {
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

	@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
	public static class ModBusClientEvents {
		@SubscribeEvent
		public static void loadCompleted(FMLLoadCompleteEvent event) {
			PonderClient.modLoadCompleted();

			ModContainer modContainer = ModList.get()
					.getModContainerById(Ponder.MOD_ID)
					.orElseThrow(() -> new IllegalStateException("Ponder Mod Container missing after loadCompleted"));

			Supplier<IConfigScreenFactory> configScreen = () ->(mc, previousScreen) -> new BaseConfigScreen(previousScreen, Ponder.MOD_ID);
			modContainer.registerExtensionPoint(IConfigScreenFactory.class, configScreen);

			BaseConfigScreen.setDefaultActionFor(Ponder.MOD_ID, base -> base
					.withButtonLabels("Client Settings", null, null)
					.withSpecs(PonderConfig.Client().specification, null, null)
			);
		}

		@SubscribeEvent
		public static void register(RegisterKeyMappingsEvent event) {
			PonderKeybinds.register(event::register);
		}
	}
}
