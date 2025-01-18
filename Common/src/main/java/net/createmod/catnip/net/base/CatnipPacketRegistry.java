package net.createmod.catnip.net.base;

import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CatnipPacketRegistry {
	public final String modId;
	public final String networkVersion;

	private final Set<PacketType<?>> packets = new HashSet<>();
	public final Set<PacketType<?>> packetsView = Collections.unmodifiableSet(packets);

	private boolean packetsRegistered = false;

	public CatnipPacketRegistry(String modId, int networkVersion) {
		this(modId, String.valueOf(networkVersion));
	}

	public CatnipPacketRegistry(String modId, String networkVersion) {
		this.modId = modId;
		this.networkVersion = networkVersion;
	}

	public void registerPacket(PacketType<?> packetType) {
		if (packetsRegistered)
			throw new IllegalStateException("Cannot register more packets after registerAllPackets() has been called!");

		packets.add(packetType);
	}

	public void registerAllPackets() {
		if (packetsRegistered)
			throw new IllegalStateException("Cannot call registerAllPackets() more than once!");

		CatnipServices.NETWORK.registerPackets(this);
		packetsRegistered = true;
	}

	public record PacketType<T extends BasePacketPayload>(CustomPacketPayload.Type<T> type, Class<T> clazz, StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {}
}
