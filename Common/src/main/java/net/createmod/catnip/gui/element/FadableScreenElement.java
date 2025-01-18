package net.createmod.catnip.gui.element;

import net.minecraft.client.gui.GuiGraphics;

@FunctionalInterface
public interface FadableScreenElement extends ScreenElement {

	@Override
	default void render(GuiGraphics graphics, int x, int y) {
		render(graphics, x, y, 1f);
	}

	void render(GuiGraphics graphics, int x, int y, float alpha);

}
