package net.createmod.catnip.api.data;

public enum TriState {
	TRUE,
	FALSE,
	DEFAULT;

	public boolean get() {
		return this == TRUE;
	}
}
