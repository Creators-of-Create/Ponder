package net.createmod.catnip.platform;

public enum Loader {
	FABRIC, NEOFORGE;

	public boolean isFabric() {
			return this == FABRIC;
	}

	public boolean isNeoForge() {
			return this == NEOFORGE;
	}

	public boolean isCurrent() {
		return this == CatnipServices.PLATFORM.getLoader();
	}
}
