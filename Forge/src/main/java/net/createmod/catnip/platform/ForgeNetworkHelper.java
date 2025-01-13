package net.createmod.catnip.platform;

import net.createmod.catnip.net.BasePacket;
import net.createmod.ponder.net.ForgePonderNetwork;
import net.createmod.catnip.platform.services.NetworkHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;

public class ForgeNetworkHelper implements NetworkHelper {

	@Override
	public void sendToPlayer(Player player, BasePacket packet) {
		if (!(player instanceof ServerPlayer serverPlayer))
			return;

		ForgePonderNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), packet);
	}

	@Override
	public void sendToNear(Level level, BlockPos pos, int range, BasePacket packet) {
		ForgePonderNetwork.CHANNEL.send(PacketDistributor.NEAR.with(
				PacketDistributor.TargetPoint.p(pos.getX(), pos.getY(), pos.getZ(), range, level.dimension())),
				packet
		);
	}

	@Override
	public void sendToEntity(Entity entity, BasePacket packet) {
		ForgePonderNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), packet);
	}

	@Override
	public void sendToServer(BasePacket packet) {
		ForgePonderNetwork.CHANNEL.sendToServer(packet);
	}
}
