package net.createmod.ponder.mixin.client.accessor;

import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientPacketListener.class)
public interface ClientPacketListenerAccessor {
	@Accessor("serverChunkRadius")
	int catnip$getServerChunkRadius();
}
