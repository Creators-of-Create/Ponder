package net.createmod.ponder;

import net.createmod.catnip.net.packets.ClientboundSimpleActionPacket;
import net.createmod.catnip.render.SuperByteBufferCache;
import net.createmod.ponder.command.SimplePonderActions;
import net.createmod.ponder.foundation.PonderIndex;
import net.createmod.ponder.foundation.content.BasePonderPlugin;
import net.createmod.ponder.foundation.content.DebugPonderPlugin;
import net.createmod.ponder.foundation.element.WorldSectionElementImpl;

public class PonderClient {

	public static final boolean ENABLE_DEBUG = true;

	public static void init() {
		SuperByteBufferCache.getInstance().registerCompartment(WorldSectionElementImpl.PONDER_WORLD_SECTION);

		ClientboundSimpleActionPacket.addAction("openPonder", () -> SimplePonderActions::openPonder);
		ClientboundSimpleActionPacket.addAction("reloadPonder", () -> SimplePonderActions::reloadPonder);

		PonderIndex.addPlugin(new BasePonderPlugin());

		if (ENABLE_DEBUG) {
			PonderIndex.addPlugin(new DebugPonderPlugin());
		}

	}

	public static void modLoadCompleted() {
		PonderIndex.registerAll();
	}

}
