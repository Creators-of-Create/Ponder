package net.createmod.catnip.net.base;

import net.createmod.catnip.annotations.ClientOnly;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;

public non-sealed interface ClientboundPacketPayload extends BasePacketPayload {
	/**
	 * Called on the main client thread.
	 * Make sure that implementations are also annotated, or else servers may crash.
	 */
	@ClientOnly
	void handle(LocalPlayer player);

	default void handleInternal(Player player) {
		if (player instanceof LocalPlayer localPlayer)
			handle(localPlayer);
	}
}
