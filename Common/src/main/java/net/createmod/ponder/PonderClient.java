package net.createmod.ponder;

import net.createmod.catnip.net.ClientboundSimpleActionPacket;
import net.createmod.catnip.render.SuperByteBufferCache;
import net.createmod.ponder.command.SimplePonderActions;
import net.createmod.ponder.foundation.PonderIndex;
import net.createmod.ponder.foundation.PonderTheme;
import net.createmod.ponder.foundation.content.DebugScenes;
import net.createmod.ponder.foundation.element.WorldSectionElement;

public class PonderClient {

	public static void init() {
		SuperByteBufferCache.getInstance().registerCompartment(WorldSectionElement.PONDER_WORLD_SECTION);

		DebugScenes.registerAll();
		PonderTheme.loadClass();
		ClientboundSimpleActionPacket.addAction("openPonder", () -> SimplePonderActions::openPonder);

	}

	public static void modLoadCompleted() {
		PonderIndex.registerAll();
	}

}
