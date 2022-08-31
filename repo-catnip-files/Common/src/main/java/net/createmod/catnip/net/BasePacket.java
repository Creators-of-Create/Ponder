package net.createmod.catnip.net;

import net.minecraft.network.FriendlyByteBuf;

public interface BasePacket {

	void write(FriendlyByteBuf buffer);

}
