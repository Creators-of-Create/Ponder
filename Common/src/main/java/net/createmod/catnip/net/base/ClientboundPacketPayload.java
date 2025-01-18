package net.createmod.catnip.net.base;

import net.minecraft.client.player.LocalPlayer;

public non-sealed interface ClientboundPacketPayload extends BasePacketPayload {
	// TODO - Something
	/**
	 * Called on the main client thread.
	 * Make sure that implementations are also annotated, or else servers may crash.
	 */
	void handle(LocalPlayer player);
}
