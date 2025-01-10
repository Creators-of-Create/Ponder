package net.createmod.catnip.net;

import javax.annotation.Nullable;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public interface ServerboundPacket extends BasePacket {

	void handle(@Nullable MinecraftServer server, @Nullable ServerPlayer player);

}
