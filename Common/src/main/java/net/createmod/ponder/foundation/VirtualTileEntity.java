package net.createmod.ponder.foundation;

/**
 * Used for simulating TE's in a client-only setting (like Ponder)
 */
public interface VirtualTileEntity {

	void markVirtual();

	boolean isVirtual();

}
