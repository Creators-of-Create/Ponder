package net.createmod.ponder.impl.client;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.createmod.catnip.api.client.gui.ScreenOpener;
import net.createmod.catnip.api.client.network.ClientNetworkHelper;
import net.createmod.ponder.api.client.PonderIndex;
import net.createmod.ponder.impl.client.gui.PonderIndexScreen;
import net.createmod.ponder.impl.client.gui.PonderTagIndexScreen;
import net.createmod.ponder.impl.client.gui.PonderUI;
import net.createmod.ponder.impl.packet.OpenPonderPacket;
import net.createmod.ponder.impl.packet.OpenPonderPacket.Target.Index;
import net.createmod.ponder.impl.packet.OpenPonderPacket.Target.Scenes;
import net.createmod.ponder.impl.packet.OpenPonderPacket.Target.Tags;
import net.createmod.ponder.impl.packet.PonderPayloads;
import net.createmod.ponder.impl.packet.ReloadPonderPacket;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;

public final class PonderClientPayloadHandlers {
	private static final Logger logger = LogUtils.getLogger();

	static void register() {
		ClientNetworkHelper.INSTANCE.registerPayloadHandler(PonderPayloads.RELOAD_PONDER, PonderClientPayloadHandlers::reloadPonder);
		ClientNetworkHelper.INSTANCE.registerPayloadHandler(PonderPayloads.OPEN_PONDER, PonderClientPayloadHandlers::openPonder);
	}

	private static void reloadPonder(ReloadPonderPacket packet, LocalPlayer player) {
		PonderIndex.reload();
	}

	private static void openPonder(OpenPonderPacket packet, LocalPlayer player) {
		switch (packet.target()) {
			case Index _ -> ScreenOpener.transitionTo(new PonderIndexScreen());
			case Tags _ -> ScreenOpener.transitionTo(new PonderTagIndexScreen());
			case Scenes(Identifier id) -> {
				if (!PonderIndex.getSceneAccess().doScenesExistForId(id)) {
					logger.warn("Ignoring OpenPonderPacket for unknown scenes {}", id);
					return;
				}

				ScreenOpener.transitionTo(PonderUI.of(id));
			}
		}
	}
}
