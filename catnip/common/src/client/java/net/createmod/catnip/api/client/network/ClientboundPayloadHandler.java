package net.createmod.catnip.api.client.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

@FunctionalInterface
public interface ClientboundPayloadHandler<T extends CustomPacketPayload> {
	void handle(T payload, Player player);
}
