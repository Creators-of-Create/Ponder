package net.createmod.catnip;


import net.createmod.catnip.config.ui.BaseConfigScreen;
import net.createmod.catnip.enums.CatnipConfig;
import net.createmod.catnip.render.SpriteShifter;
import net.createmod.catnip.utility.placement.PlacementClient;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;

public class ForgeCatnipClient {

	public static final IModelData NoModelData = new ModelDataMap.Builder().build();

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
		public static void onRenderWorld(RenderLevelLastEvent event) {
			CatnipClient.onRenderWorld(event.getPoseStack());
		}

		@SubscribeEvent
		public static void onLoadWorld(WorldEvent.Load event) {
			CatnipClient.onLoadWorld(event.getWorld());
		}

		@SubscribeEvent
		public static void onUnloadWorld(WorldEvent.Unload event) {
			CatnipClient.onUnloadWorld(event.getWorld());
		}

		@SubscribeEvent
		public static void afterRenderOverlayLayer(RenderGameOverlayEvent.PostLayer event) {
			if (event.getOverlay() != ForgeIngameGui.CROSSHAIR_ELEMENT)
				return;

			PlacementClient.onRenderCrosshairOverlay(event.getWindow(), event.getMatrixStack(), event.getPartialTicks());
		}

	}

	@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
	public static class ModBusClientEvents {
		@SubscribeEvent
		public static void loadCompleted(FMLLoadCompleteEvent event) {
			ModContainer modContainer = ModList.get()
					.getModContainerById(Catnip.MOD_ID)
					.orElseThrow(() -> new IllegalStateException("Catnip Mod Container missing after loadCompleted"));
			modContainer.registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class,
					() -> new ConfigGuiHandler.ConfigGuiFactory(
							(mc, previousScreen) -> new BaseConfigScreen(previousScreen, Catnip.MOD_ID)));

			BaseConfigScreen.setDefaultActionFor(Catnip.MOD_ID, base -> base
					.withTitles("Client Settings", null, null)
					.withSpecs(CatnipConfig.Client().specification, null, null)
			);
		}

		@SubscribeEvent
		public static void registerClientReloadListeners(RegisterClientReloadListenersEvent event) {
			event.registerReloadListener(CatnipClient.RESOURCE_RELOAD_LISTENER);
		}

		@SubscribeEvent
		public static void onTextureStitchPre(TextureStitchEvent.Pre event) {
			if (!event.getAtlas().location().equals(InventoryMenu.BLOCK_ATLAS))
				return;

			SpriteShifter.getAllTargetSprites().forEach(event::addSprite);
		}

		@SubscribeEvent
		public static void onTextureStitchPost(TextureStitchEvent.Post event) {
			if (!event.getAtlas().location().equals(InventoryMenu.BLOCK_ATLAS))
				return;

			SpriteShifter.getAllShifts().forEach(entry -> entry.loadTextures(event.getAtlas()));
		}
	}

}
