package net.createmod.ponder;

import java.util.Optional;
import java.util.function.Supplier;

import net.createmod.catnip.config.ui.BaseConfigScreen;
import net.createmod.catnip.render.StitchedSprite;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.placement.PlacementClient;
import net.createmod.catnip.theme.Color;
import net.createmod.ponder.enums.PonderConfig;
import net.createmod.ponder.enums.PonderKeybinds;
import net.createmod.ponder.foundation.PonderTooltipHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;

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
			if (event.phase != TickEvent.Phase.END)
				return;

			PonderClient.onTick();
			PonderTooltipHandler.tick();
		}

		@SubscribeEvent
		public static void onRenderWorld(RenderLevelStageEvent event) {
			if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES)
				return;

			PonderClient.onRenderWorld(event.getPoseStack());
		}

		@SubscribeEvent
		public static void onLoadWorld(LevelEvent.Load event) {
			PonderClient.onLoadWorld(event.getLevel());
		}

		@SubscribeEvent
		public static void onUnloadWorld(LevelEvent.Unload event) {
			PonderClient.onUnloadWorld(event.getLevel());
		}

		@SubscribeEvent
		public static void afterRenderOverlayLayer(RenderGuiOverlayEvent.Post event) {
			if (event.getOverlay() != VanillaGuiOverlay.CROSSHAIR.type())
				return;

			PlacementClient.onRenderCrosshairOverlay(event.getWindow(), event.getGuiGraphics(), event.getPartialTick());
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
		public static void registerClientReloadListeners(RegisterClientReloadListenersEvent event) {
			event.registerReloadListener(PonderClient.RESOURCE_RELOAD_LISTENER);
		}

		@SubscribeEvent
		public static void onTextureStichPost(TextureStitchEvent.Post event) {
			StitchedSprite.onTextureStitchPost(event.getAtlas());
		}

		@SubscribeEvent
		public static void register(RegisterKeyMappingsEvent event) {
			PonderKeybinds.register(event::register);
		}
	}
}
