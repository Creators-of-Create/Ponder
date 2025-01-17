package net.createmod.catnip.event;

import net.createmod.catnip.lang.LangNumberFormat;
import net.createmod.ponder.PonderClient;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

public class ClientResourceReloadListener implements ResourceManagerReloadListener {

	@Override
	public void onResourceManagerReload(ResourceManager resourceManager) {
		LangNumberFormat.numberFormat.update();
		PonderClient.invalidateRenderers();
	}

}
