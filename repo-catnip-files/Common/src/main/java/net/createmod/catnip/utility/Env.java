package net.createmod.catnip.utility;

public enum Env {
	CLIENT, SERVER;

	public boolean isClient() {
		return this == CLIENT;
	}

	public boolean isServer() {
		return this == SERVER;
	}
}
