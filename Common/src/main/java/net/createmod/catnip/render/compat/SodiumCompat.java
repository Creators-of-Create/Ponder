package net.createmod.catnip.render.compat;

import net.createmod.catnip.platform.CatnipServices;

public final class SodiumCompat {
	private SodiumCompat() {
	}

	public static final boolean IS_SODIUM_INSTALLED = CatnipServices.PLATFORM.isModLoaded("sodium");
}
