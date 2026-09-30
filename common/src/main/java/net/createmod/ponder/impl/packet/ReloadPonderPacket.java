package net.createmod.ponder.impl.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public enum ReloadPonderPacket implements CustomPacketPayload {
	INSTANCE;

	public static final StreamCodec<ByteBuf, ReloadPonderPacket> CODEC = StreamCodec.unit(INSTANCE);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PonderPayloads.RELOAD_PONDER;
	}
}
