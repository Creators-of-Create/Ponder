package net.createmod.catnip.impl.neoforge;

import net.createmod.catnip.api.Catnip;
import net.createmod.catnip.api.event.ServerCommandRegistrationCallback;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@Mod(Catnip.ID)
@EventBusSubscriber
public final class CatnipNeoforge {
	public CatnipNeoforge(IEventBus bus) {
		Catnip.preInit();
		bus.addListener(CatnipNeoforge::setup);
	}

	public static void setup(FMLCommonSetupEvent event) {
		event.enqueueWork(Catnip::init);
	}

	@SubscribeEvent
	public static void registerCommands(RegisterCommandsEvent event) {
		ServerCommandRegistrationCallback.EVENT.invoker().register(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection());
	}
}
