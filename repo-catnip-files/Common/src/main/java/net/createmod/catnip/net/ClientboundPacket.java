package net.createmod.catnip.net;

public interface ClientboundPacket extends BasePacket {

	// can't include handle method in the packet class due to classloading
	// so instead create a static inner class that has the handle method
	// found this approach implemented in botania, so thanks to them

}
