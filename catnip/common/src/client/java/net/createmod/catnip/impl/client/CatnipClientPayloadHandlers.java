package net.createmod.catnip.impl.client;

import net.createmod.catnip.api.config.ConfigId;
import net.createmod.catnip.api.config.ConfigValueId;
import net.createmod.catnip.impl.client.gui.config.RootConfigScreen;
import net.createmod.catnip.impl.client.gui.config.ConfigScreen;
import net.createmod.catnip.impl.config.ConfigHelper;
import net.createmod.catnip.impl.config.packet.ClientboundSetConfigValuePacket;
import net.createmod.catnip.impl.config.packet.OpenConfigScreenPacket;

import net.createmod.catnip.impl.config.packet.OpenConfigScreenPacket.Target.Config;
import net.createmod.catnip.impl.config.packet.OpenConfigScreenPacket.Target.Mod;
import net.createmod.catnip.impl.config.packet.OpenConfigScreenPacket.Target.Value;
import net.minecraft.client.Minecraft;

import net.minecraft.client.gui.screens.Screen;

import net.minecraft.network.chat.Component;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.createmod.catnip.api.client.network.ClientNetworkHelper;
import net.createmod.catnip.impl.network.CatnipPayloads;
import net.minecraft.client.player.LocalPlayer;

public final class CatnipClientPayloadHandlers {
	private static final Logger logger = LogUtils.getLogger();

	public static void register() {
		ClientNetworkHelper.INSTANCE.registerPayloadHandler(CatnipPayloads.OPEN_CONFIG_SCREEN, CatnipClientPayloadHandlers::openConfigScreen);
		ClientNetworkHelper.INSTANCE.registerPayloadHandler(CatnipPayloads.SET_CONFIG_VALUE, CatnipClientPayloadHandlers::setConfigValue);
	}

	private static void openConfigScreen(OpenConfigScreenPacket packet, LocalPlayer player) {
		Minecraft mc = Minecraft.getInstance();
		Screen currentScreen = mc.screen;

		switch (packet.target()) {
			case Mod(String id) -> RootConfigScreen.forMod(id, currentScreen).ifPresentOrElse(
				mc::setScreen, () -> logger.warn("Failed to open RootConfigScreen for mod {}", id)
			);
			case Config(ConfigId id) -> ConfigScreen.find(id).ifPresentOrElse(
				mc::setScreen, () -> logger.warn("Failed to open ConfigScreen for config {}", id)
			);
			case Value(ConfigValueId id) -> ConfigScreen.find(id).ifPresentOrElse(
				mc::setScreen, () -> logger.warn("Failed to open ConfigScreen for value {}", id)
			);
		}
	}

	private static void setConfigValue(ClientboundSetConfigValuePacket packet, LocalPlayer player) {
		// FIXME: translation
		ConfigHelper.trySetValue(packet.id(), packet.value()).ifPresentOrElse(
			error -> logger.warn("Config value update failed: {}", error),
			() -> player.sendSystemMessage(Component.literal("Config value " + packet.id() + " has been updated by the server"))
		);
	}
}
