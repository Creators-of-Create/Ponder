package net.createmod.catnip;


import net.createmod.catnip.utility.placement.PlacementHelpers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class ForgeCatnipClient {

	@Mod.EventBusSubscriber(Dist.CLIENT)
	public static class ClientEvents {

		@SubscribeEvent
		public static void onTick(TickEvent.ClientTickEvent event) {
			CatnipClient.onTick();
		}

		@SubscribeEvent
		public static void onRenderWorld(RenderLevelLastEvent event) {
			CatnipClient.onRenderWorld(event.getPoseStack());
		}

		@SubscribeEvent
		public static void afterRenderOverlayLayer(RenderGameOverlayEvent.PostLayer event) {
			if (event.getOverlay() != ForgeIngameGui.CROSSHAIR_ELEMENT)
				return;

			PlacementHelpers.onRenderCrosshairOverlay(event.getWindow(), event.getMatrixStack(), event.getPartialTicks());
		}

	}

}
