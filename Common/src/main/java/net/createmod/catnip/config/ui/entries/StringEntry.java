package net.createmod.catnip.config.ui.entries;

import net.createmod.catnip.config.ui.ConfigTextField;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraftforge.common.ForgeConfigSpec;

public class StringEntry extends ValueEntry<String> {

	protected EditBox textField;

	public StringEntry(String label, ForgeConfigSpec.ConfigValue<String> value, ForgeConfigSpec.ValueSpec spec) {
		super(label, value, spec);
		textField = new ConfigTextField(Minecraft.getInstance().font, 0, 0, 200, 20);
		textField.setValue(value.get());

		textField.setResponder(this::setValue);

		textField.moveCursorToStart();
		listeners.add(textField);
		onReset();
	}

	@Override
	public void render(GuiGraphics graphics, int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean p_230432_9_, float partialTicks) {
		super.render(graphics, index, y, x, width, height, mouseX, mouseY, p_230432_9_, partialTicks);

		textField.setX(x + width - 82 - resetWidth);
		textField.setY(y + 8);
		textField.setWidth(Math.min(width - getLabelWidth(width) - resetWidth, 60));
		textField.render(graphics, mouseX, mouseY, partialTicks);

	}

	@Override
	protected void setEditable(boolean b) {
		super.setEditable(b);
		textField.setEditable(b);
	}

	@Override
	public void tick() {
		super.tick();
		textField.tick();
	}
}
