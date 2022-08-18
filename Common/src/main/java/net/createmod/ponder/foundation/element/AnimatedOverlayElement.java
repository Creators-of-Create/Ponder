package net.createmod.ponder.foundation.element;

import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.catnip.utility.animation.LerpedFloat;
import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.ui.PonderUI;

public abstract class AnimatedOverlayElement extends PonderOverlayElement {

	protected LerpedFloat fade;

	public AnimatedOverlayElement() {
		fade = LerpedFloat.linear()
			.startWithValue(0);
	}

	public void setFade(float fade) {
		this.fade.setValue(fade);
	}

	@Override
	public final void render(PonderScene scene, PonderUI screen, PoseStack ms, float partialTicks) {
		float currentFade = fade.getValue(partialTicks);
		render(scene, screen, ms, partialTicks, currentFade);
	}

	protected abstract void render(PonderScene scene, PonderUI screen, PoseStack ms, float partialTicks, float fade);

}
