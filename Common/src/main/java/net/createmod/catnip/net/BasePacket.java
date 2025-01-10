package net.createmod.catnip.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public interface BasePacket {

	void write(FriendlyByteBuf buffer);

	ResourceLocation getId();

}
