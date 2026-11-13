package net.createmod.catnip.impl.fabric.client;

import net.createmod.catnip.api.client.command.ClientCommands;
import net.createmod.catnip.api.client.event.ClientTickCallback;
import net.createmod.catnip.api.client.event.LevelRenderCallback;
import net.createmod.catnip.api.client.event.LevelRendererReloadCallback;
import net.createmod.catnip.impl.client.CatnipClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.InvalidateRenderStateCallback;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;

public final class CatnipFabricClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		CatnipClient.preInit();
		CatnipClient.init();

		// event registration
		ClientTickEvents.START_CLIENT_TICK.register(_ -> ClientTickCallback.EVENT.pre().invoker().onTick());
		ClientTickEvents.END_CLIENT_TICK.register(_ -> ClientTickCallback.EVENT.post().invoker().onTick());
		InvalidateRenderStateCallback.EVENT.register(LevelRendererReloadCallback.EVENT.invoker()::onReload);
		LevelRenderEvents.AFTER_TRANSLUCENT_FEATURES.register(context -> LevelRenderCallback.AFTER_TRANSLUCENT_FEATURES.invoker().onRender(
			context.levelRenderer(), context.levelState(), context.poseStack()
		));
		ClientCommandRegistrationCallback.EVENT.register(ClientCommands::registerCommands);
	}
}
