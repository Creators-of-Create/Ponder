package net.createmod.catnip.platform;

public enum Loader {
	FABRIC, FORGE;

	public boolean isFabric() {
			return this == FABRIC;
	}

	public boolean isForge() {
			return this == FORGE;
	}

	public boolean isCurrent() {
		return this == CatnipServices.PLATFORM.getLoader();
	}
}
