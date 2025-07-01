package net.createmod.catnip.platform;

import net.createmod.catnip.platform.services.ExternalRenderHelper;
import net.createmod.catnip.platform.services.ModClientHooksHelper;

public class CatnipClientServices extends CatnipServices {

	public static final ModClientHooksHelper CLIENT_HOOKS = load(ModClientHooksHelper.class);
	public static final ExternalRenderHelper EXTERNAL_RENDER_HELPER = load(ExternalRenderHelper.class);
}
