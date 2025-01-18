package net.createmod.catnip.render;

import net.createmod.catnip.theme.Color;
import net.minecraft.client.gui.GuiGraphics;

public interface ColoredRenderable {

	void render(GuiGraphics graphics, int x, int y, Color c);

}
