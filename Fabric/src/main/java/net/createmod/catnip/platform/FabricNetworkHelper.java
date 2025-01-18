package net.createmod.catnip.platform;

import net.createmod.catnip.net.base.CatnipPacketRegistry;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.createmod.catnip.platform.services.NetworkHelper;
import net.createmod.ponder.FabricPonder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.ApiStatus;

public class FabricNetworkHelper implements NetworkHelper {
	@ApiStatus.Internal
	@Override
	public void registerPackets(CatnipPacketRegistry packetRegistry) {
		for (CatnipPacketRegistry.PacketType<?> type : packetRegistry.packetsView) {
			boolean clientbound = ClientboundPacketPayload.class.isAssignableFrom(type.clazz());
			boolean serverbound = ServerboundPacketPayload.class.isAssignableFrom(type.clazz());
			if (clientbound && serverbound) {
				throw new IllegalStateException("Packet class is both clientbound and serverbound: " + type.clazz());
			} else if (clientbound) {
				CatnipPacketRegistry.PacketType<ClientboundPacketPayload> casted = (CatnipPacketRegistry.PacketType<ClientboundPacketPayload>) type;
				PayloadTypeRegistry.playS2C().register(casted.type(), casted.codec());
				ClientPlayNetworking.registerGlobalReceiver(casted.type(), (payload, ctx) -> payload.handle(ctx.player()));
			} else if (serverbound) {
				CatnipPacketRegistry.PacketType<ServerboundPacketPayload> casted = (CatnipPacketRegistry.PacketType<ServerboundPacketPayload>) type;
				PayloadTypeRegistry.playC2S().register(casted.type(), casted.codec());
				ServerPlayNetworking.registerGlobalReceiver(casted.type(), ((payload, ctx) -> payload.handle(ctx.player())));
			}
		}
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void sendToServer(CustomPacketPayload payload) {
		ClientPlayNetworking.send(payload);
	}

	@Override
	public void sendToClient(ServerPlayer player, CustomPacketPayload payload) {
		ServerPlayNetworking.send(player, payload);
	}

	@Override
	public void sendToAllClients(CustomPacketPayload payload) {
		Packet<?> packet = ServerPlayNetworking.createS2CPacket(payload);
		FabricPonder.getServer().getPlayerList().broadcastAll(packet);
	}

	@Override
	public void sendToClientsTrackingAndSelf(Entity entity, CustomPacketPayload payload) {
		Packet<?> packet = ServerPlayNetworking.createS2CPacket(payload);
		if (entity.level().getChunkSource() instanceof ServerChunkCache chunkCache) {
			chunkCache.broadcastAndSend(entity, packet);
		} else {
			throw new IllegalStateException("Cannot send clientbound payloads on the client");
		}
	}

	@Override
	public void sendToClientsTrackingEntity(Entity entity, CustomPacketPayload payload) {
		Packet<?> packet = ServerPlayNetworking.createS2CPacket(payload);
		if (entity.level().getChunkSource() instanceof ServerChunkCache chunkCache) {
			chunkCache.broadcast(entity, packet);
		} else {
			throw new IllegalStateException("Cannot send clientbound payloads on the client");
		}
	}

	@Override
	public void sendToClientsTrackingChunk(ServerLevel serverLevel, ChunkPos chunk, CustomPacketPayload payload) {
		for (ServerPlayer player : serverLevel.getChunkSource().chunkMap.getPlayers(chunk, false)) {
			sendToClient(player, payload);
		}
	}

	@Override
	public void sendToClientsAround(ServerLevel serverLevel, Vec3 pos, double radius, CustomPacketPayload payload) {
		Packet<?> packet = ServerPlayNetworking.createS2CPacket(payload);
		serverLevel.getServer().getPlayerList().broadcast(null, pos.x(), pos.y(), pos.z(), radius, serverLevel.dimension(), packet);
	}
}
