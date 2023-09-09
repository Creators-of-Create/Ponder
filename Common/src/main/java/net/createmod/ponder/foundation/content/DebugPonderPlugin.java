package net.createmod.ponder.foundation.content;

import net.createmod.ponder.Ponder;
import net.createmod.ponder.foundation.api.registration.PonderPlugin;
import net.createmod.ponder.foundation.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

public class DebugPonderPlugin implements PonderPlugin {

	@Override
	public String getModId() {
		return Ponder.MOD_ID;
	}

	@Override
	public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
		DebugScenes.registerAll(helper);
	}
}
