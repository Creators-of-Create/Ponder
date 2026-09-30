package net.createmod.ponder.fabric;

import net.createmod.ponder.api.Ponder;
import net.fabricmc.api.ModInitializer;

public class PonderFabric implements ModInitializer {
	@Override
	public void onInitialize() {
		Ponder.init();
	}
}
