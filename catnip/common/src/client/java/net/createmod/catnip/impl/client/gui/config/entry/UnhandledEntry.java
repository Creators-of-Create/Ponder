package net.createmod.catnip.impl.client.gui.config.entry;

import net.createmod.catnip.api.config.ConfigPath;
import net.createmod.catnip.api.config.access.ConfigAccess;
import net.createmod.catnip.impl.client.gui.config.entry.list.ConfigEntryList;
import net.minecraft.client.gui.components.events.GuiEventListener;

import java.util.function.Consumer;

public final class UnhandledEntry extends ConfigEntryList.Entry {
	public UnhandledEntry(ConfigEntryList list, ConfigPath path, ConfigAccess.Value<?> value) {
		super(list, path, value);
	}

	@Override
	protected void collectChildren(Consumer<GuiEventListener> output) {}
}
