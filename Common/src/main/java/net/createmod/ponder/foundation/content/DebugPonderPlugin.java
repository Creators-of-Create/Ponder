package net.createmod.ponder.foundation.content;

import net.createmod.ponder.Ponder;
import net.createmod.ponder.foundation.PonderPlugin;

public class DebugPonderPlugin implements PonderPlugin {

	@Override
	public String getModID() {
		return Ponder.MOD_ID;
	}

	@Override
	public void registerScenes() {
		DebugScenes.registerAll();
	}
}
