package net.createmod.catnip.impl.client;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.createmod.catnip.api.client.network.ClientNetworkHelper;
import net.createmod.catnip.impl.network.CatnipPayloads;
import net.createmod.catnip.impl.network.ClientboundConfigPacket;
import net.createmod.catnip.impl.network.ClientboundSimpleActionPacket;
import net.minecraft.world.entity.player.Player;

public final class CatnipClientPayloadHandlers {
	private static final Logger logger = LogUtils.getLogger();

	public static void register() {
		ClientNetworkHelper.INSTANCE.registerPayloadHandler(CatnipPayloads.CLIENTBOUND_CONFIG, CatnipClientPayloadHandlers::config);
		ClientNetworkHelper.INSTANCE.registerPayloadHandler(CatnipPayloads.SIMPLE_ACTION, CatnipClientPayloadHandlers::action);
	}

	private static void config(ClientboundConfigPacket payload, Player player) {/*
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
			player.displayClientMessage(Component.literal("Great Success!"), false);
		} catch (ConfigHelper.InvalidValueException e) {
			player.displayClientMessage(Component.literal("Config could not be set the the specified value!"), false);
		} catch (Exception e) {
			player.displayClientMessage(Component.literal("Something went wrong while trying to set config value. Check the client logs for more information"), false);
			Ponder.LOGGER.warn("Exception during client-side config value set:", e);
		}

	*/}

	private static void action(ClientboundSimpleActionPacket payload, Player player) {
		String name = payload.action();
		Supplier<Consumer<String>> action = ClientboundSimpleActionPacket.ACTIONS.get(name);

		if (action == null) {
			logger.warn("Received ClientboundSimpleActionPacket with invalid Action {}, ignoring the packet", name);
			return;
		}

		action.get().accept(payload.value());
	}
}
