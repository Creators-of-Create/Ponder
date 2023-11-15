package net.createmod.ponder.api.element;

import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.ui.PonderUI;
import net.minecraft.client.gui.GuiGraphics;

public interface PonderOverlayElement extends PonderElement {

    void render(PonderScene scene, PonderUI screen, GuiGraphics graphics, float partialTicks);

}
