package net.createmod.catnip.net.packets;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.config.ui.ConfigHelper;
import net.createmod.catnip.lang.Components;
import net.createmod.catnip.net.CatnipPackets;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.createmod.ponder.Ponder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.fml.config.ModConfig;

public record ClientboundConfigPacket(String path, String value) implements ClientboundPacketPayload {
	public static final StreamCodec<ByteBuf, ClientboundConfigPacket> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.STRING_UTF8, ClientboundConfigPacket::path,
		ByteBufCodecs.STRING_UTF8, ClientboundConfigPacket::value,
		ClientboundConfigPacket::new
	);

	@Override
	public PacketTypeProvider getTypeProvider() {
		return CatnipPackets.CLIENTBOUND_CONFIG;
	}

	@Override
	public void handle(LocalPlayer player) {
		if (Minecraft.getInstance().player == null) {
			return;
		}

		ConfigHelper.ConfigPath path;

		try {
			path = ConfigHelper.ConfigPath.parse(this.path);
		} catch (IllegalArgumentException e) {
			player.displayClientMessage(Ponder.lang().text(e.getMessage()).component(), false);
			return;
		}

		if (path.getType() != ModConfig.Type.CLIENT) {
			Ponder.LOGGER.warn("Received type-mismatched config packet on client");
			return;
		}

		try {
			ConfigHelper.setConfigValue(path, value);
			player.displayClientMessage(Components.literal("Great Success!"), false);
		} catch (ConfigHelper.InvalidValueException e) {
			player.displayClientMessage(Components.literal("Config could not be set the the specified value!"), false);
		} catch (Exception e) {
			player.displayClientMessage(Components.literal("Something went wrong while trying to set config value. Check the client logs for more information"), false);
			Ponder.LOGGER.warn("Exception during client-side config value set:", e);
		}

	}
}
