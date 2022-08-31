package net.createmod.catnip;


import net.createmod.catnip.config.ui.BaseConfigScreen;
import net.createmod.catnip.enums.CatnipConfig;
import net.createmod.catnip.utility.AnimationTickHolder;
import net.createmod.catnip.utility.placement.PlacementClient;
import net.createmod.catnip.utility.worldWrappers.WrappedClientWorld;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.client.gui.ForgeIngameGui;
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
			CatnipClient.onTick();
			AnimationTickHolder.tick();
		}

		@SubscribeEvent
		public static void onRenderWorld(RenderLevelLastEvent event) {
			CatnipClient.onRenderWorld(event.getPoseStack());
		}

		@SubscribeEvent
		public static void onLoadWorld(WorldEvent.Load event) {
			LevelAccessor world = event.getWorld();
			if (!world.isClientSide() || !(world instanceof ClientLevel) || world instanceof WrappedClientWorld)
				return;

			AnimationTickHolder.reset();
		}

		@SubscribeEvent
		public static void onUnloadWorld(WorldEvent.Unload event) {
			if (!event.getWorld().isClientSide())
				return;

			AnimationTickHolder.reset();
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
	}

}
