package net.createmod.ponder.api.element;

import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.ui.PonderUI;

public interface PonderOverlayElement extends PonderElement {

    void render(PonderScene scene, PonderUI screen, PoseStack ms, float partialTicks);

}
