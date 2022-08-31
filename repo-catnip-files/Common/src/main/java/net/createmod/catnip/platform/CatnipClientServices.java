package net.createmod.catnip.platform;

import net.createmod.catnip.platform.services.ModClientHooksHelper;

public class CatnipClientServices extends CatnipServices {

	public static final ModClientHooksHelper CLIENT_HOOKS = load(ModClientHooksHelper.class);

}
