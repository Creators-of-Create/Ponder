package net.createmod.ponder.fabric.client;

import net.createmod.ponder.api.client.event.TooltipQueryCallback;
import net.createmod.ponder.impl.client.PonderClient;
import net.createmod.ponder.impl.client.PonderKeybinds;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.Minecraft;

public class PonderFabricClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		PonderClient.init();

		ItemTooltipCallback.EVENT.register(TooltipQueryCallback.EVENT.invoker()::onTooltipQuery);
		PonderKeybinds.register(KeyMappingHelper::registerKeyMapping);

		ClientLifecycleEvents.CLIENT_STARTED.register(PonderFabricClient::onClientStarted);
	}

	private static void onClientStarted(Minecraft client) {
		PonderClient.modLoadCompleted();
	}
}
