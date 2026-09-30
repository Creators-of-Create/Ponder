package net.createmod.ponder.neoforge;

import net.createmod.ponder.api.Ponder;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(Ponder.MOD_ID)
public class PonderNeoforge {
	public PonderNeoforge(IEventBus bus) {
		bus.addListener(PonderNeoforge::setup);
	}

	public static void setup(FMLCommonSetupEvent event) {
		event.enqueueWork(Ponder::init);
	}
}
