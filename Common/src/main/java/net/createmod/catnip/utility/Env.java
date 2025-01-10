package net.createmod.catnip.utility;

import net.createmod.catnip.platform.CatnipServices;

public enum Env {
	CLIENT, SERVER;

	public boolean isClient() {
		return this == CLIENT;
	}

	public boolean isServer() {
		return this == SERVER;
	}

	public boolean isCurrent() {
		return this == CatnipServices.PLATFORM.getEnv();
	}
}
