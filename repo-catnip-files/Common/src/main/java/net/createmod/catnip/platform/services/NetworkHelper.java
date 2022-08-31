package net.createmod.catnip.platform.services;

import net.createmod.catnip.net.BasePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public interface NetworkHelper {

	void sendToPlayer(Player player, BasePacket packet);

	void sendToNear(Level level, BlockPos pos, int range, BasePacket packet);

	void sendToEntity(Entity entity, BasePacket packet);

	void sendToServer(BasePacket packet);

}
