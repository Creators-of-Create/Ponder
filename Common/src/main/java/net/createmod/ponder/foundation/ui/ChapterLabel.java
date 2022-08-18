package net.createmod.ponder.foundation.ui;

import java.util.function.BiConsumer;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.catnip.gui.UIRenderHelper;
import net.createmod.catnip.gui.widget.AbstractSimiWidget;
import net.createmod.catnip.utility.theme.Theme;
import net.createmod.ponder.foundation.PonderChapter;
import net.minecraft.client.Minecraft;

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
	public void render(@Nonnull PoseStack ms, int mouseX, int mouseY, float partialTicks) {
		UIRenderHelper.streak(ms, 0, x, y + height / 2, height - 2, width);
		Minecraft.getInstance().font.draw(ms, chapter.getTitle(), x + 50,
			y + 20, Theme.Key.TEXT_ACCENT_SLIGHT.i());

		button.renderButton(ms, mouseX, mouseY, partialTicks);
		super.render(ms, mouseX, mouseY, partialTicks);
	}

	@Override
	public void onClick(double x, double y) {
		if (!button.isMouseOver(x, y))
			return;

		button.runCallback(x, y);
	}
}
