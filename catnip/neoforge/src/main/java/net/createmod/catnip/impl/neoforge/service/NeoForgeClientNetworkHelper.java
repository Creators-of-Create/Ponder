package net.createmod.catnip.impl.neoforge.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.createmod.catnip.api.client.network.ClientNetworkHelper;
import net.createmod.catnip.api.client.network.ClientboundPayloadHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;

public class NeoForgeClientNetworkHelper implements ClientNetworkHelper {
	private static final Set<String> registeredNamespaces = Collections.synchronizedSet(new HashSet<>());
	private final Map<Type<?>, ClientboundPayloadHandler<?>> handlers = Collections.synchronizedMap(new HashMap<>());

	@Override
	public <T extends CustomPacketPayload> void registerPayloadHandler(Type<T> type, ClientboundPayloadHandler<T> handler) {
		this.handlers.put(type, handler);
		if (registeredNamespaces.add(type.id().getNamespace())) {
			ModContainer container = ModList.get().getModContainerById(type.id().getNamespace()).orElseThrow();
			container.getEventBus().addListener((RegisterClientPayloadHandlersEvent e) -> {
				registerClientPayloads(e, type.id().getNamespace());
			});
		}
	}

	private void registerClientPayloads(RegisterClientPayloadHandlersEvent e, String namespace) {
		this.handlers.forEach((type, handler) -> {
			if (!type.id().getNamespace().equals(namespace))
				return;

			e.register(type, ((payload, context) -> {
				((ClientboundPayloadHandler) handler).handle(payload, context.player());
			}));
		});
	}

	@Override
	public void sendToServer(CustomPacketPayload payload) {
		Minecraft.getInstance().getConnection().send(payload);
	}
}
