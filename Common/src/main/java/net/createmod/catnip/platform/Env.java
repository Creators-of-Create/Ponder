package net.createmod.catnip.platform;

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
