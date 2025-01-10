package net.createmod.catnip;


import net.createmod.catnip.config.ui.BaseConfigScreen;
import net.createmod.catnip.enums.CatnipConfig;
import net.createmod.catnip.render.StitchedSprite;
import net.createmod.catnip.utility.placement.PlacementClient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;

public class ForgeCatnipClient {

	public static void onCtor(IEventBus modEventBus, IEventBus forgeEventBus) {
		modEventBus.addListener(ForgeCatnipClient::init);
	}

	public static void init(final FMLClientSetupEvent event) {
		CatnipClient.init();
	}

	@Mod.EventBusSubscriber(Dist.CLIENT)
	public static class ClientEvents {

		@SubscribeEvent
		public static void onTick(TickEvent.ClientTickEvent event) {
			if (event.phase == TickEvent.Phase.START)
				return;

			CatnipClient.onTick();
		}

		@SubscribeEvent
		public static void onRenderWorld(RenderLevelStageEvent event) {
			if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES)
				return;

			CatnipClient.onRenderWorld(event.getPoseStack());
		}

		@SubscribeEvent
		public static void onLoadWorld(LevelEvent.Load event) {
			CatnipClient.onLoadWorld(event.getLevel());
		}

		@SubscribeEvent
		public static void onUnloadWorld(LevelEvent.Unload event) {
			CatnipClient.onUnloadWorld(event.getLevel());
		}

		@SubscribeEvent
		public static void afterRenderOverlayLayer(RenderGuiOverlayEvent.Post event) {
			if (event.getOverlay() != VanillaGuiOverlay.CROSSHAIR.type())
				return;

			PlacementClient.onRenderCrosshairOverlay(event.getWindow(), event.getGuiGraphics(), event.getPartialTick());
		}

	}

	@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
	public static class ModBusClientEvents {
		@SubscribeEvent
		public static void loadCompleted(FMLLoadCompleteEvent event) {
			ModContainer modContainer = ModList.get()
					.getModContainerById(Catnip.MOD_ID)
					.orElseThrow(() -> new IllegalStateException("Catnip Mod Container missing after loadCompleted"));
			modContainer.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
					() -> new ConfigScreenHandler.ConfigScreenFactory(
							(mc, previousScreen) -> new BaseConfigScreen(previousScreen, Catnip.MOD_ID)));

			BaseConfigScreen.setDefaultActionFor(Catnip.MOD_ID, base -> base
					.withButtonLabels("Client Settings", null, null)
					.withSpecs(CatnipConfig.Client().specification, null, null)
			);
		}

		@SubscribeEvent
		public static void registerClientReloadListeners(RegisterClientReloadListenersEvent event) {
			event.registerReloadListener(CatnipClient.RESOURCE_RELOAD_LISTENER);
		}

		@SubscribeEvent
		public static void onTextureStichPost(TextureStitchEvent.Post event) {
			StitchedSprite.onTextureStitchPost(event.getAtlas());
		}
	}

}
