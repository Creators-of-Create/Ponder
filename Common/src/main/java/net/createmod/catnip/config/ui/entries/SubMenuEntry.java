package net.createmod.catnip.config.ui.entries;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import net.createmod.catnip.config.ui.ConfigScreenList;
import net.createmod.catnip.config.ui.SubMenuConfigScreen;
import net.createmod.catnip.gui.ScreenOpener;
import net.createmod.catnip.gui.element.DelegatedStencilElement;
import net.createmod.catnip.gui.widget.BoxWidget;
import net.createmod.ponder.enums.PonderGuiTextures;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.neoforge.common.ModConfigSpec;

public class SubMenuEntry extends ConfigScreenList.LabeledEntry {

	protected BoxWidget button;

	public SubMenuEntry(SubMenuConfigScreen parent, String label, ModConfigSpec spec, UnmodifiableConfig config) {
		super(label);

		button = new BoxWidget(0, 0, 35, 16)
				.showingElement(PonderGuiTextures.ICON_CONFIG_OPEN.asStencil().at(10, 0))
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
	public void render(GuiGraphics graphics, int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean p_230432_9_, float partialTicks) {
		super.render(graphics, index, y, x, width, height, mouseX, mouseY, p_230432_9_, partialTicks);

		button.setX(x + width - 108);
		button.setY(y + 10);
		button.setHeight(height - 20);
		button.render(graphics, mouseX, mouseY, partialTicks);
	}

	@Override
	protected int getLabelWidth(int totalWidth) {
		return (int) (totalWidth * labelWidthMult) + 30;
	}
}
