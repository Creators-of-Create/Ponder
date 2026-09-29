package net.createmod.catnip.impl.client.gui.config.entry.value;

import net.createmod.catnip.api.config.ConfigPath;
import net.createmod.catnip.api.config.access.ConfigAccess;
import net.createmod.catnip.impl.client.gui.config.entry.list.ConfigEntryList;
import net.createmod.catnip.impl.client.gui.config.entry.ValueEntry;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;

import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;

import net.minecraft.network.chat.MutableComponent;

import java.util.function.Consumer;

public final class StringEntry extends ValueEntry<String> {
	private final EditBox textField;

	public StringEntry(ConfigEntryList list, ConfigPath path, ConfigAccess.Value<String> value) {
		super(list, path, value);

		// FIXME: translation
		MutableComponent narration = Component.literal("Edit box for " + this.name());
		textField = new EditBox(list.screen.getFont(), 0, 0, 200, 20, narration);
		textField.setValue(this.getValue());
		textField.setResponder(this::setValue);
		textField.moveCursorToStart(false);
		textField.setEditable(list.screen.isMutable());
	}

	@Override
	protected void collectChildren(Consumer<GuiEventListener> output) {
		super.collectChildren(output);
		output.accept(this.textField);
	}

	@Override
	public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
		super.extractContent(graphics, mouseX, mouseY, hovered, partialTick);

		textField.setX(getX() + getWidth() - 82 - resetWidth);
		textField.setY(getY() + 8);
		textField.setWidth(Math.min(getWidth() - getLabelWidth(getWidth()) - resetWidth, 60));
		textField.extractWidgetRenderState(graphics, mouseX, mouseY, partialTick);
	}
}
