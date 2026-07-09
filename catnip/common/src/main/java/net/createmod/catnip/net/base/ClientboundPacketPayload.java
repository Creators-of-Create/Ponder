package net.createmod.catnip.net.base;

import net.minecraft.world.entity.player.Player;

public interface ClientboundPacketPayload extends BasePacketPayload {
	void handle(Player player);
}
