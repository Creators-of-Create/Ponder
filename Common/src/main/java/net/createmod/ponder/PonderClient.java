package net.createmod.ponder;

import net.createmod.catnip.net.ClientboundSimpleActionPacket;
import net.createmod.ponder.command.SimplePonderActions;
import net.createmod.ponder.foundation.PonderIndex;
import net.createmod.ponder.foundation.PonderTheme;
import net.createmod.ponder.foundation.content.DebugScenes;

public class PonderClient {

	public static void init() {
		DebugScenes.registerAll();
		PonderTheme.loadClass();
		ClientboundSimpleActionPacket.addAction("openPonder", () -> SimplePonderActions::openPonder);

	}

	public static void modLoadCompleted() {
		PonderIndex.registerAll();
	}

}
