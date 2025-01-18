package net.createmod.catnip.net.base;

import net.minecraft.server.level.ServerPlayer;

public non-sealed interface ServerboundPacketPayload extends BasePacketPayload {
	/**
	 * Called on the main client thread.
	 */
	void handle(ServerPlayer player);
}
