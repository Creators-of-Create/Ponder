package net.createmod.catnip;

import io.github.fabricators_of_create.porting_lib.event.client.ClientWorldEvents;
import io.github.fabricators_of_create.porting_lib.event.client.OverlayRenderCallback;
import net.createmod.catnip.config.ui.BaseConfigScreen;
import net.createmod.catnip.enums.CatnipConfig;
import net.createmod.catnip.utility.FabricClientResourceReloadListener;
import net.createmod.catnip.utility.placement.PlacementClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.packs.PackType;

public class FabricCatnipClient implements ClientModInitializer {

	public static final FabricClientResourceReloadListener FABRIC$RESOURCE_RELOAD_LISTENER = new FabricClientResourceReloadListener();

	@Override
	public void onInitializeClient() {
		CatnipClient.init();
		ClientTickEvents.END_CLIENT_TICK.register(client -> CatnipClient.onTick());
		ClientWorldEvents.LOAD.register((client, world) -> CatnipClient.onLoadWorld(world));
		ClientWorldEvents.UNLOAD.register((client, world) -> CatnipClient.onUnloadWorld(world));
		WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> CatnipClient.onRenderWorld(context.matrixStack()));
		OverlayRenderCallback.EVENT.register((stack, partialTicks, window, type) -> {
			if (type != OverlayRenderCallback.Types.CROSSHAIRS)
				return false;
			PlacementClient.onRenderCrosshairOverlay(window, stack, partialTicks);
			return false;
		});

		ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(FABRIC$RESOURCE_RELOAD_LISTENER);

		prepareConfigUI();
	}

	private void prepareConfigUI() {
		BaseConfigScreen.setDefaultActionFor(Catnip.MOD_ID, base -> base
				.withButtonLabels("Client Settings", null, null)
				.withSpecs(CatnipConfig.Client().specification, null, null)
		);
	}
}
