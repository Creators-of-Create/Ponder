package net.createmod.catnip.net;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;

public class FabricCatnipNetwork {
	// Purely here to prevent class loading
	public static void sendToClient(BasePacket packet) {
		FriendlyByteBuf buf = PacketByteBufs.create();
		packet.write(buf);
		ClientPlayNetworking.send(packet.getId(), buf);
	}
}
