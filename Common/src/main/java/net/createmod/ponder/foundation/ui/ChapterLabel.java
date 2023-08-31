package net.createmod.ponder.foundation.ui;

import net.createmod.catnip.gui.UIRenderHelper;
import net.createmod.catnip.gui.widget.AbstractSimiWidget;
import net.createmod.catnip.utility.theme.Theme;
import net.createmod.ponder.foundation.PonderChapter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.function.BiConsumer;

public class ChapterLabel extends AbstractSimiWidget {

	private final PonderChapter chapter;
	private final PonderButton button;

	public ChapterLabel(PonderChapter chapter, int x, int y, BiConsumer<Integer, Integer> onClick) {
		super(x, y, 175, 38);

		this.button = new PonderButton(x + 4, y + 4, 30, 30)
				.showing(chapter)
				.withCallback(onClick);

		this.chapter = chapter;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		UIRenderHelper.streak(graphics, 0, getX(), getY() + height / 2, height - 2, width);
		graphics.drawString(Minecraft.getInstance().font, chapter.getTitle(), getX() + 50,
			getY() + 20, Theme.Key.TEXT_ACCENT_SLIGHT.i());

		button.renderButton(graphics, mouseX, mouseY, partialTicks);
		super.render(graphics, mouseX, mouseY, partialTicks);
	}

	@Override
	public void onClick(double x, double y) {
		if (!button.isMouseOver(x, y))
			return;

		button.runCallback(x, y);
	}
}
