package net.createmod.catnip.platform.services;

import net.createmod.catnip.net.base.CatnipPacketRegistry;
import net.createmod.catnip.net.packets.ClientboundSimpleActionPacket;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.ApiStatus;

public interface NetworkHelper {
	@ApiStatus.Internal
	void registerPackets(CatnipPacketRegistry packetRegistry);

	void sendToServer(CustomPacketPayload payload);

	void sendToClient(ServerPlayer player, CustomPacketPayload payload);

	default void sendToClients(Iterable<ServerPlayer> players, CustomPacketPayload payload) {
		for (ServerPlayer player : players) {
			sendToClient(player, payload);
		}
	}

	void sendToAllClients(CustomPacketPayload payload);

	void sendToClientsTrackingAndSelf(Entity entity, CustomPacketPayload payload);

	void sendToClientsTrackingEntity(Entity entity, CustomPacketPayload payload);

	void sendToClientsTrackingChunk(ServerLevel serverLevel, ChunkPos chunk, CustomPacketPayload payload);

	void sendToClientsAround(ServerLevel serverLevel, Vec3 pos, double radius, CustomPacketPayload payload);

	default void sendToClientsAround(ServerLevel serverLevel, Vec3i pos, double radius, CustomPacketPayload payload) {
		sendToClientsAround(serverLevel, new Vec3(pos.getX(), pos.getY(), pos.getZ()), radius, payload);
	}

	default void simpleActionToClient(ServerPlayer player, String action, String value) {
		sendToClient(player, new ClientboundSimpleActionPacket(action, value));
	}
}
