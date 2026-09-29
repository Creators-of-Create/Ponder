package net.createmod.catnip.impl.client.gui.config.entry.value;

import net.createmod.catnip.api.client.gui.UIRenderHelper;
import net.createmod.catnip.api.client.gui.element.RenderElement;
import net.createmod.catnip.api.client.gui.texture.CatnipGuiTextures;
import net.createmod.catnip.api.client.gui.widget.AbstractSimiWidget;
import net.createmod.catnip.api.client.gui.widget.BoxWidget;
import net.createmod.catnip.api.config.ConfigPath;
import net.createmod.catnip.api.config.access.ConfigAccess;
import net.createmod.catnip.impl.client.gui.config.entry.list.ConfigEntryList;
import net.createmod.catnip.impl.client.gui.config.entry.ValueEntry;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;

import java.util.function.Consumer;

public final class BooleanEntry extends ValueEntry<Boolean> {
	private final RenderElement enabled;
	private final RenderElement disabled;
	private final BoxWidget button;

	public BooleanEntry(ConfigEntryList list, ConfigPath path, ConfigAccess.Value<Boolean> value) {
		super(list, path, value);

		enabled = CatnipGuiTextures.ICON_CONFIRM.asStencil()
			.withElementRenderer((ms, width, height, _) -> UIRenderHelper.angledGradient(ms, 0, 0, height / 2, height, width, AbstractSimiWidget.COLOR_SUCCESS))
			.at(10, 0);

		disabled = CatnipGuiTextures.ICON_DISABLE.asStencil()
			.withElementRenderer((ms, width, height, _) -> UIRenderHelper.angledGradient(ms, 0, 0, height / 2, height, width, AbstractSimiWidget.COLOR_FAIL))
			.at(10, 0);

		RenderElement currentDisplay = this.getValue() ? enabled : disabled;
		button = new BoxWidget().showingElement(currentDisplay).withCallback(() -> setValue(!getValue()));
		button.active = list.screen.isMutable();
	}

	@Override
	protected void collectChildren(Consumer<GuiEventListener> output) {
		super.collectChildren(output);
		output.accept(this.button);
	}

	@Override
	public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
		super.extractContent(graphics, mouseX, mouseY, hovered, partialTick);

		button.setX(getX() + getWidth() - 80 - resetWidth);
		button.setY(getY() + 10);
		button.setWidth(35);
		button.setHeight(getHeight() - 20);
		button.extractRenderState(graphics, mouseX, mouseY, partialTick);
	}

	@Override
	protected void onValueChange(Boolean newValue, ChangeCause cause) {
		super.onValueChange(newValue, cause);
		button.showingElement(newValue ? enabled : disabled);

		if (cause == ChangeCause.SELF) {
			this.bumpCog(newValue ? 15f : -16f);
		}
	}
}
