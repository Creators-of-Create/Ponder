package net.createmod.catnip.net.base;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface BasePacketPayload extends CustomPacketPayload {
	PacketTypeProvider getTypeProvider();

	@Override
	default CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return getTypeProvider().getType();
	}

	interface PacketTypeProvider {
		<T extends CustomPacketPayload> CustomPacketPayload.Type<T> getType();
	}
}
