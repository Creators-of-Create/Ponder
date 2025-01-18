package net.createmod.catnip.config.ui.entries;

import net.createmod.catnip.gui.UIRenderHelper;
import net.createmod.catnip.gui.element.RenderElement;
import net.createmod.catnip.gui.widget.AbstractSimiWidget;
import net.createmod.catnip.gui.widget.BoxWidget;
import net.createmod.ponder.enums.PonderGuiTextures;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.common.ForgeConfigSpec;

public class BooleanEntry extends ValueEntry<Boolean> {

	RenderElement enabled;
	RenderElement disabled;
	BoxWidget button;

	public BooleanEntry(String label, ForgeConfigSpec.ConfigValue<Boolean> value, ForgeConfigSpec.ValueSpec spec) {
		super(label, value, spec);

		enabled = PonderGuiTextures.ICON_CONFIRM.asStencil()
			.withElementRenderer((ms, width, height, alpha) -> UIRenderHelper.angledGradient(ms, 0, 0, height / 2, height, width, AbstractSimiWidget.COLOR_SUCCESS))
			.at(10, 0);

		disabled = PonderGuiTextures.ICON_DISABLE.asStencil()
			.withElementRenderer((ms, width, height, alpha) -> UIRenderHelper.angledGradient(ms, 0, 0, height / 2, height, width, AbstractSimiWidget.COLOR_FAIL))
			.at(10, 0);

		button = new BoxWidget().showingElement(enabled)
			.withCallback(() -> setValue(!getValue()));

		listeners.add(button);
		onReset();
	}

	@Override
	protected void setEditable(boolean b) {
		super.setEditable(b);
		button.active = b;
	}

	@Override
	public void tick() {
		super.tick();
		button.tick();
	}

	@Override
	public void render(GuiGraphics graphics, int index, int y, int x, int width, int height, int mouseX, int mouseY,
					   boolean p_230432_9_, float partialTicks) {
		super.render(graphics, index, y, x, width, height, mouseX, mouseY, p_230432_9_, partialTicks);

		button.setX(x + width - 80 - resetWidth);
		button.setY(y + 10);
		button.setWidth(35);
		button.setHeight(height - 20);
		button.render(graphics, mouseX, mouseY, partialTicks);
	}

	@Override
	public void onValueChange(Boolean newValue) {
		super.onValueChange(newValue);
		button.showingElement(newValue ? enabled : disabled);
		bumpCog(newValue ? 15f : -16f);
	}
}
