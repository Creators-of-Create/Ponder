package net.createmod.catnip.platform;

import net.createmod.catnip.net.BasePacket;
import net.createmod.catnip.net.FabricCatnipNetwork;
import net.createmod.catnip.platform.services.NetworkHelper;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class FabricNetworkHelper implements NetworkHelper {
	@Override
	public void sendToPlayer(Player player, BasePacket packet) {
		if (!(player instanceof ServerPlayer serverPlayer))
			return;

		FriendlyByteBuf buf = PacketByteBufs.create();
		packet.write(buf);
		ServerPlayNetworking.send(serverPlayer, packet.getId(), buf);
	}

	@Override
	public void sendToNear(Level level, BlockPos pos, int range, BasePacket packet) {
		if (!(level instanceof ServerLevel serverLevel))
			return;
		PlayerLookup.around(serverLevel, pos, range).forEach(player -> {
			FriendlyByteBuf buf = PacketByteBufs.create();
			packet.write(buf);
			ServerPlayNetworking.send(player, packet.getId(), buf);
		});
	}

	@Override
	public void sendToEntity(Entity entity, BasePacket packet) {
		FriendlyByteBuf buf = PacketByteBufs.create();
		packet.write(buf);
		if (entity instanceof ServerPlayer serverPlayer)
			ServerPlayNetworking.send(serverPlayer, packet.getId(), buf);
		PlayerLookup.tracking(entity).forEach(player -> ServerPlayNetworking.send(player, packet.getId(), buf));
	}

	@Override
	public void sendToServer(BasePacket packet) {
		FabricCatnipNetwork.sendToClient(packet);
	}
}
