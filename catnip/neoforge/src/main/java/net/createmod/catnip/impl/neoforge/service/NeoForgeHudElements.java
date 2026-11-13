package net.createmod.catnip.impl.neoforge.service;

import java.util.ArrayList;
import java.util.List;

import net.createmod.catnip.api.client.gui.HudElements;
import net.minecraft.resources.Identifier;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.GuiLayer;

@EventBusSubscriber
public final class NeoForgeHudElements implements HudElements {
	private static final List<Registration> registrations = new ArrayList<>();

	@Override
	public synchronized void register(Identifier id, Element element) {
		registrations.add(new Registration(id, element::render));
	}

	@SubscribeEvent
	public static void registerEvent(RegisterGuiLayersEvent event) {
		registrations.forEach(registration -> event.registerAboveAll(registration.id, registration.layer));
	}

	private record Registration(Identifier id, GuiLayer layer) {}
}
