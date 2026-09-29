package net.createmod.catnip.impl.client.gui.config.entry;

import net.createmod.catnip.api.client.gui.element.DelegatedStencilElement;
import net.createmod.catnip.api.client.gui.texture.CatnipGuiTextures;
import net.createmod.catnip.api.client.gui.widget.BoxWidget;
import net.createmod.catnip.api.config.ConfigPath;
import net.createmod.catnip.api.config.access.ConfigAccess;
import net.createmod.catnip.impl.client.gui.config.entry.list.ConfigEntryList;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import net.minecraft.client.gui.components.events.GuiEventListener;

import java.util.function.Consumer;

public final class NestedEntry extends ConfigEntryList.Entry {
	private final BoxWidget button;

	public NestedEntry(ConfigEntryList list, ConfigPath path, ConfigAccess.Nested nested) {
		super(list, path, nested);

		this.button = new BoxWidget(0, 0, 35, 16)
			.showingElement(CatnipGuiTextures.ICON_CONFIG_OPEN.asStencil().at(10, 0))
			.withCallback(() -> list.screen.navigate(this.id.path()));
		this.button.modifyElement(e -> ((DelegatedStencilElement) e).withElementRenderer(BoxWidget.GRADIENT_FACTORY.apply(button)));
	}

	@Override
	public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
		super.extractContent(graphics, mouseX, mouseY, hovered, partialTick);

		button.setX(getX() + getWidth() - 108);
		button.setY(getY() + 10);
		button.setHeight(getHeight() - 20);
		button.extractRenderState(graphics, mouseX, mouseY, partialTick);
	}

	@Override
	protected void collectChildren(Consumer<GuiEventListener> output) {
		output.accept(this.button);
	}
}
