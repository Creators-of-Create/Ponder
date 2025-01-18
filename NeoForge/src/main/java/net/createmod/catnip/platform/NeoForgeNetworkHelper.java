package net.createmod.catnip.platform;

import net.createmod.catnip.net.base.CatnipPacketRegistry;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.createmod.catnip.platform.services.NetworkHelper;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import org.jetbrains.annotations.ApiStatus;

public class NeoForgeNetworkHelper implements NetworkHelper {
	@ApiStatus.Internal
	@Override
	public void registerPackets(CatnipPacketRegistry packetRegistry) {
		ModContainer container = ModList.get().getModContainerById(packetRegistry.modId).orElseThrow();
		container.getEventBus().addListener((RegisterPayloadHandlersEvent e) -> {
			PayloadRegistrar registrar = e.registrar(packetRegistry.networkVersion);

			for (CatnipPacketRegistry.PacketType<?> type : packetRegistry.packetsView) {
				boolean clientbound = ClientboundPacketPayload.class.isAssignableFrom(type.clazz());
				boolean serverbound = ServerboundPacketPayload.class.isAssignableFrom(type.clazz());
				if (clientbound && serverbound) {
					throw new IllegalStateException("Packet class is both clientbound and serverbound: " + type.clazz());
				} else if (clientbound) {
					CatnipPacketRegistry.PacketType<ClientboundPacketPayload> casted = (CatnipPacketRegistry.PacketType<ClientboundPacketPayload>) type;
					// GameTestServer somehow manages to call this on the server
					if (CatnipServices.PLATFORM.getEnv().isClient())
						registrar.playToClient(casted.type(), casted.codec(), (payload, ctx) -> {
							ctx.enqueueWork(() -> {
								payload.handle((LocalPlayer) ctx.player());
							});
						});
				} else if (serverbound) {
					CatnipPacketRegistry.PacketType<ServerboundPacketPayload> casted = (CatnipPacketRegistry.PacketType<ServerboundPacketPayload>) type;
					registrar.playToServer(casted.type(), casted.codec(), (payload, ctx) -> {
						ctx.enqueueWork(() -> {
							payload.handle((ServerPlayer) ctx.player());
						});
					});
				}
			}
		});
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void sendToServer(CustomPacketPayload payload) {
		PacketDistributor.sendToServer(payload);
	}

	@Override
	public void sendToClient(ServerPlayer player, CustomPacketPayload payload) {
		PacketDistributor.sendToPlayer(player, payload);
	}

	@Override
	public void sendToAllClients(CustomPacketPayload payload) {
		PacketDistributor.sendToAllPlayers(payload);
	}

	@Override
	public void sendToClientsTrackingAndSelf(Entity entity, CustomPacketPayload payload) {
		PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, payload);
	}

	@Override
	public void sendToClientsTrackingEntity(Entity entity, CustomPacketPayload payload) {
		PacketDistributor.sendToPlayersTrackingEntity(entity, payload);
	}

	@Override
	public void sendToClientsTrackingChunk(ServerLevel serverLevel, ChunkPos chunk, CustomPacketPayload payload) {
		PacketDistributor.sendToPlayersTrackingChunk(serverLevel, chunk, payload);
	}

	@Override
	public void sendToClientsAround(ServerLevel serverLevel, Vec3 pos, double radius, CustomPacketPayload payload) {
		PacketDistributor.sendToPlayersNear(serverLevel, null, pos.x(), pos.y(), pos.z(), radius, payload);
	}
}
