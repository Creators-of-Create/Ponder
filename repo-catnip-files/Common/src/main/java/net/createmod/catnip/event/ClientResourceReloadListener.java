package net.createmod.catnip.event;

import net.createmod.catnip.CatnipClient;
import net.createmod.catnip.utility.lang.LangNumberFormat;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

public class ClientResourceReloadListener implements ResourceManagerReloadListener {

	@Override
	public void onResourceManagerReload(ResourceManager resourceManager) {
		LangNumberFormat.numberFormat.update();
		CatnipClient.invalidateRenderers();
	}

}
