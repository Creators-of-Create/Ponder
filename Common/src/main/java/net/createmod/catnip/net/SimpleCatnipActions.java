package net.createmod.catnip.net;

import net.createmod.catnip.config.ui.ConfigHelper;
import net.createmod.catnip.config.ui.ConfigModListScreen;
import net.createmod.catnip.config.ui.SubMenuConfigScreen;
import net.createmod.catnip.gui.ScreenOpener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

public class SimpleCatnipActions {

	public static void configScreen(String value) {
		if (value.equals("")) {
			ScreenOpener.open(new ConfigModListScreen(null));
			return;
		}

		LocalPlayer player = Minecraft.getInstance().player;

		if (player == null)
			return;

		ConfigHelper.ConfigPath configPath;
		try {
			configPath = ConfigHelper.ConfigPath.parse(value);
		} catch (IllegalArgumentException e) {
			player.displayClientMessage(Component.literal(e.getMessage()), false);
			return;
		}

		try {
			ScreenOpener.open(SubMenuConfigScreen.find(configPath));
		} catch (Exception e) {
			player.displayClientMessage(Component.literal("Unable to find the specified config"), false);
		}
	}

}
