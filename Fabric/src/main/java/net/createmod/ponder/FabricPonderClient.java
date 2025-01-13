package net.createmod.ponder;

import java.util.Optional;

import io.github.fabricators_of_create.porting_lib.event.client.ClientWorldEvents;
import io.github.fabricators_of_create.porting_lib.event.client.OverlayRenderCallback;
import io.github.fabricators_of_create.porting_lib.event.client.RenderTooltipBorderColorCallback;
import net.createmod.catnip.config.ui.BaseConfigScreen;
import net.createmod.catnip.utility.Couple;
import net.createmod.ponder.utility.FabricClientResourceReloadListener;
import net.createmod.catnip.utility.placement.PlacementClient;
import net.createmod.catnip.utility.theme.Color;
import net.createmod.ponder.enums.PonderConfig;
import net.createmod.ponder.enums.PonderKeybinds;
import net.createmod.ponder.foundation.PonderTooltipHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.ItemStack;

public class FabricPonderClient implements ClientModInitializer {

	public static final FabricClientResourceReloadListener FABRIC$RESOURCE_RELOAD_LISTENER = new FabricClientResourceReloadListener();

	@Override
	public void onInitializeClient() {
		PonderClient.init();

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			PonderClient.onTick();
			PonderTooltipHandler.tick();
		});

		ClientWorldEvents.LOAD.register((client, world) -> PonderClient.onLoadWorld(world));
		ClientWorldEvents.UNLOAD.register((client, world) -> PonderClient.onUnloadWorld(world));

		WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> PonderClient.onRenderWorld(context.matrixStack()));

		OverlayRenderCallback.EVENT.register((stack, partialTicks, window, type) -> {
			if (type != OverlayRenderCallback.Types.CROSSHAIRS)
				return false;

			PlacementClient.onRenderCrosshairOverlay(window, stack, partialTicks);
			return false;
		});

		ItemTooltipCallback.EVENT.register((stack, context, lines) -> PonderTooltipHandler.addToTooltip(lines, stack));
		RenderTooltipBorderColorCallback.EVENT.register(FabricPonderClient::getItemTooltipColor);
		PonderKeybinds.register(KeyBindingHelper::registerKeyBinding);

		ClientLifecycleEvents.CLIENT_STARTED.register(FabricPonderClient::onClientStarted);

		ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(FABRIC$RESOURCE_RELOAD_LISTENER);

		prepareConfigUI();
	}

	private void prepareConfigUI() {
		BaseConfigScreen.setDefaultActionFor(Ponder.MOD_ID, base -> base
				.withButtonLabels("Client Settings", null, null)
				.withSpecs(PonderConfig.Client().specification, null, null)
		);
	}

	private static void onClientStarted(Minecraft client) {
		PonderClient.modLoadCompleted();
	}

	public static RenderTooltipBorderColorCallback.BorderColorEntry getItemTooltipColor(ItemStack stack, int originalBorderColorStart, int originalBorderColorEnd) {
		Optional<Couple<Color>> colors = PonderTooltipHandler.handleTooltipColor(stack);
		return new RenderTooltipBorderColorCallback.BorderColorEntry(
				colors.map(c -> c.getFirst().getRGB()).orElse(originalBorderColorStart),
				colors.map(c -> c.getSecond().getRGB()).orElse(originalBorderColorEnd)
		);
	}
}
