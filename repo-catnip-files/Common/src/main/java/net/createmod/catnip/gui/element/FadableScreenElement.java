package net.createmod.catnip.gui.element;

import com.mojang.blaze3d.vertex.PoseStack;

@FunctionalInterface
public interface FadableScreenElement extends ScreenElement {

	@Override
	default void render(PoseStack ms, int x, int y) {
		render(ms, x, y, 1f);
	}

	void render(PoseStack ms, int x, int y, float alpha);

}
