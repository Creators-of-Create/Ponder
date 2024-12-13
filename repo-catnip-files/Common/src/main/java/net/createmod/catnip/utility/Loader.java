package net.createmod.catnip.utility;

public enum Loader {
	FABRIC, FORGE;

	public boolean isFabric() {
			return this == FABRIC;
	}

	public boolean isForge() {
			return this == FORGE;
	}
}
