package net.createmod.catnip.impl.config.packet;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.api.config.ConfigValueId;
import net.createmod.catnip.impl.network.CatnipPayloads;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ClientboundSetConfigValuePacket(ConfigValueId id, String value) implements CustomPacketPayload {
	public static final StreamCodec<ByteBuf, ClientboundSetConfigValuePacket> CODEC = StreamCodec.composite(
		ConfigValueId.STREAM_CODEC, ClientboundSetConfigValuePacket::id,
		ByteBufCodecs.STRING_UTF8, ClientboundSetConfigValuePacket::value,
		ClientboundSetConfigValuePacket::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return CatnipPayloads.SET_CONFIG_VALUE;
	}
}
