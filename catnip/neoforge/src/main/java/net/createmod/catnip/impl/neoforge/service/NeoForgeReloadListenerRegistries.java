package net.createmod.catnip.impl.neoforge.service;

import java.util.ArrayList;
import java.util.List;

import net.createmod.catnip.api.data.ReloadListenerRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.SortedReloadListenerEvent;

@EventBusSubscriber
public record NeoForgeReloadListenerRegistries(Registry assets, Registry data) implements ReloadListenerRegistries {
	@SuppressWarnings("unused") // will be invoked by ServiceLoader
	public NeoForgeReloadListenerRegistries() {
		this(new NeoforgeRegistry(), new NeoforgeRegistry());
	}

	@SubscribeEvent
	public static void registerAssets(AddClientReloadListenersEvent event) {
		((NeoforgeRegistry) ReloadListenerRegistries.INSTANCE.assets()).register(event);
	}

	@SubscribeEvent
	public static void registerData(AddServerReloadListenersEvent event) {
		((NeoforgeRegistry) ReloadListenerRegistries.INSTANCE.data()).register(event);
	}

	private static final class NeoforgeRegistry implements Registry {
		// use a list instead of a map so neo can handle duplicate checking for us
		private final List<Registration> registrations = new ArrayList<>();
		private final List<Ordering> orderings = new ArrayList<>();

		@Override
		public void register(Identifier id, PreparableReloadListener listener) {
			this.registrations.add(new Registration(id, listener));
		}

		@Override
		public void addOrdering(Identifier before, Identifier after) {
			this.orderings.add(new Ordering(before, after));
		}

		private void register(SortedReloadListenerEvent event) {
			this.registrations.forEach(registration -> event.addListener(registration.id, registration.listener));
			this.orderings.forEach(ordering -> event.addDependency(ordering.before, ordering.after));
		}

		private record Registration(Identifier id, PreparableReloadListener listener) {}
		private record Ordering(Identifier before, Identifier after) {}
	}
}
