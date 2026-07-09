package net.createmod.catnip.net.base;

import net.createmod.catnip.api.network.SelfHandlingPayload;
import net.minecraft.server.level.ServerPlayer;

public interface ServerboundPacketPayload extends BasePacketPayload, SelfHandlingPayload {
	@Override
	void handle(ServerPlayer player);
}
