package net.createmod.catnip.api.client.config.entries;

import net.createmod.catnip.api.client.config.ConfigTextField;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;

import net.neoforged.neoforge.common.ModConfigSpec;

public class StringEntry extends ValueEntry<String> {

	protected EditBox textField;

	public StringEntry(String label, ModConfigSpec.ConfigValue<String> value, ModConfigSpec.ValueSpec spec) {
		super(label, value, spec);
		textField = new ConfigTextField(Minecraft.getInstance().font, 0, 0, 200, 20);
		textField.setValue(value.get());

		textField.setResponder(this::setValue);

		textField.moveCursorToStart(false);
		listeners.add(textField);
		onReset();
	}

	@Override
	public void renderContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean isHovering, float partialTick) {
		super.renderContent(graphics, mouseX, mouseY, isHovering, partialTick);

		textField.setX(getX() + getWidth() - 82 - resetWidth);
		textField.setY(getY() + 8);
		textField.setWidth(Math.min(getWidth() - getLabelWidth(getWidth()) - resetWidth, 60));
		textField.extractRenderState(graphics, mouseX, mouseY, partialTick);

	}

	@Override
	protected void setEditable(boolean b) {
		super.setEditable(b);
		textField.setEditable(b);
	}
}
