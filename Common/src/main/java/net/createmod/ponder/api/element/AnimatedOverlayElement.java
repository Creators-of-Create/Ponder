package net.createmod.ponder.api.element;

import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.ui.PonderUI;

public interface AnimatedOverlayElement extends PonderOverlayElement {

	void setFade(float fade);

	float getFade(float partialTicks);

	@Override
	default void render(PonderScene scene, PonderUI screen, PoseStack ms, float partialTicks) {
		render(scene, screen, ms, partialTicks, getFade(partialTicks));
	}

	void render(PonderScene scene, PonderUI screen, PoseStack ms, float partialTicks, float fade);
}
