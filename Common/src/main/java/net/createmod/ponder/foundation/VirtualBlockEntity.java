package net.createmod.ponder.foundation;

/**
 * Used for simulating BE's in a client-only setting (like Ponder)
 */
public interface VirtualBlockEntity {

	void markVirtual();

	boolean isVirtual();

}
