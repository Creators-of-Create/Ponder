package net.createmod.ponder.foundation.element;

import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.ui.PonderUI;
import net.minecraft.client.gui.GuiGraphics;

public abstract class PonderOverlayElement extends PonderElement {

	public void tick(PonderScene scene) {}

	public abstract void render(PonderScene scene, PonderUI screen, GuiGraphics graphics, float partialTicks);

}
