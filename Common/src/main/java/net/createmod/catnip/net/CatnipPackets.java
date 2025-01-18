package net.createmod.catnip.net;

import java.util.Locale;

import net.createmod.catnip.net.base.BasePacketPayload;
import net.createmod.catnip.net.base.CatnipPacketRegistry;
import net.createmod.catnip.net.packets.ClientboundConfigPacket;
import net.createmod.catnip.net.packets.ClientboundSimpleActionPacket;
import net.createmod.catnip.net.packets.ServerboundConfigPacket;
import net.createmod.ponder.Ponder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public enum CatnipPackets implements BasePacketPayload.PacketTypeProvider {
	// C2S
	SERVERBOUND_CONFIG(ServerboundConfigPacket.class, ServerboundConfigPacket.STREAM_CODEC),

	// S2C
	CLIENTBOUND_SIMPLE_ACTION(ClientboundSimpleActionPacket.class, ClientboundSimpleActionPacket.STREAM_CODEC),
	CLIENTBOUND_CONFIG(ClientboundConfigPacket.class, ClientboundConfigPacket.STREAM_CODEC);

	private final CatnipPacketRegistry.PacketType<?> type;

	<T extends BasePacketPayload> CatnipPackets(Class<T> clazz, StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
		String name = this.name().toLowerCase(Locale.ROOT);
		this.type = new CatnipPacketRegistry.PacketType<>(
			new CustomPacketPayload.Type<>(Ponder.asResource(name)),
			clazz, codec
		);
	}


	@Override
	public <T extends CustomPacketPayload> CustomPacketPayload.Type<T> getType() {
		//noinspection unchecked
		return (CustomPacketPayload.Type<T>) type.type();
	}

	public static void register() {
		CatnipPacketRegistry registry = new CatnipPacketRegistry(Ponder.MOD_ID, 1);

		for (CatnipPackets packet : values()) {
			registry.registerPacket(packet.type);
		}

		registry.registerAllPackets();
	}
}
