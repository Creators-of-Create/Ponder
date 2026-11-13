package net.createmod.catnip.impl.fabric;

import net.createmod.catnip.api.Catnip;
import net.createmod.catnip.api.event.ServerCommandRegistrationCallback;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public final class CatnipFabric implements ModInitializer {
	@Override
	public void onInitialize() {
		Catnip.preInit();
		Catnip.init();
		CommandRegistrationCallback.EVENT.register(ServerCommandRegistrationCallback.EVENT.invoker()::register);
	}
}
