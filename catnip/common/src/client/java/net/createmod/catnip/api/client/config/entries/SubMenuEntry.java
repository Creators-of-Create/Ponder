package net.createmod.catnip.api.client.config.entries;

import com.electronwill.nightconfig.core.UnmodifiableConfig;

import net.createmod.catnip.api.client.gui.ScreenOpener;
import net.createmod.catnip.api.client.gui.element.DelegatedStencilElement;
import net.createmod.catnip.api.client.gui.widget.BoxWidget;
import net.createmod.catnip.api.client.config.ConfigScreenList;
import net.createmod.catnip.api.client.config.SubMenuConfigScreen;
import net.createmod.catnip.api.client.gui.texture.CatnipGuiTextures;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import net.neoforged.neoforge.common.ModConfigSpec;

public class SubMenuEntry extends ConfigScreenList.LabeledEntry {

	protected BoxWidget button;

	public SubMenuEntry(SubMenuConfigScreen parent, String label, ModConfigSpec spec, UnmodifiableConfig config) {
		super(label);

		button = new BoxWidget(0, 0, 35, 16)
			.showingElement(CatnipGuiTextures.ICON_CONFIG_OPEN.asStencil().at(10, 0))
			.withCallback(() -> ScreenOpener.open(new SubMenuConfigScreen(parent, label, parent.type, spec, config)));
		button.modifyElement(e -> ((DelegatedStencilElement) e).withElementRenderer(BoxWidget.gradientFactory.apply(button)));

		listeners.add(button);
	}

	@Override
	public void tick() {
		super.tick();
		button.tick();
	}

	@Override
	public void renderContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean isHovering, float partialTick) {
		super.renderContent(graphics, mouseX, mouseY, isHovering, partialTick);

		button.setX(getX() + getWidth() - 108);
		button.setY(getY() + 10);
		button.setHeight(getHeight() - 20);
		button.render(graphics, mouseX, mouseY, partialTick);
	}

	@Override
	protected int getLabelWidth(int totalWidth) {
		return (int) (totalWidth * labelWidthMult) + 30;
	}
}
