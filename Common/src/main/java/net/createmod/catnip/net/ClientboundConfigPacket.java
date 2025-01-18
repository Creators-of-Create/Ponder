package net.createmod.catnip.net;

import net.createmod.catnip.config.ui.ConfigHelper;
import net.createmod.catnip.lang.Components;
import net.createmod.ponder.Ponder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.config.ModConfig;

public class ClientboundConfigPacket implements ClientboundPacket {

	public static final ResourceLocation ID = Ponder.asResource("config_packet");

	private final String path;
	private final String value;

	public ClientboundConfigPacket(String path, String value) {
		this.path = path;
		this.value = value;
	}

	public ClientboundConfigPacket(FriendlyByteBuf buffer) {
		this.path = buffer.readUtf();
		this.value = buffer.readUtf();
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeUtf(path);
		buffer.writeUtf(value);
	}

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	public static class Handler {
		public static void handle(ClientboundConfigPacket packet) {
			if (Minecraft.getInstance().player == null) {
				return;
			}

			Minecraft.getInstance().execute(() -> {
				LocalPlayer player = Minecraft.getInstance().player;
				ConfigHelper.ConfigPath path;

				try {
					path = ConfigHelper.ConfigPath.parse(packet.path);
				} catch (IllegalArgumentException e) {
					player.displayClientMessage(Ponder.lang().text(e.getMessage()).component(), false);
					return;
				}

				if (path.getType() != ModConfig.Type.CLIENT) {
					Ponder.LOGGER.warn("Received type-mismatched config packet on client");
					return;
				}

				try {
					ConfigHelper.setConfigValue(path, packet.value);
					player.displayClientMessage(Components.literal("Great Success!"), false);
				} catch (ConfigHelper.InvalidValueException e) {
					player.displayClientMessage(Components.literal("Config could not be set the the specified value!"), false);
				} catch (Exception e) {
					player.displayClientMessage(Components.literal("Something went wrong while trying to set config value. Check the client logs for more information"), false);
					Ponder.LOGGER.warn("Exception during client-side config value set:", e);
				}
			});
		}
	}
}
