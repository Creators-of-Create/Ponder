package net.createmod.catnip.net.base;

import net.createmod.catnip.api.client.network.ClientNetworkHelper;

final class ClientboundPacketRegistryBridge {
	private ClientboundPacketRegistryBridge() {
	}

	static <T extends BasePacketPayload> void register(CatnipPacketRegistry.PacketType<T> packet) {
		if (!ClientboundPacketPayload.class.isAssignableFrom(packet.clazz()))
			return;
		ClientNetworkHelper.INSTANCE.registerPayloadHandler(packet.type(),
			(payload, player) -> ((ClientboundPacketPayload) payload).handle(player));
	}
}
