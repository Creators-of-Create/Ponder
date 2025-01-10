package net.createmod.catnip.gui.element;

import net.minecraft.client.gui.GuiGraphics;

public interface RenderElement extends FadableScreenElement {

	static RenderElement of(ScreenElement renderable) {
		return new AbstractRenderElement.SimpleRenderElement(renderable);
	}

	<T extends RenderElement> T at(float x, float y);

	<T extends RenderElement> T at(float x, float y, float z);

	<T extends RenderElement> T withBounds(int width, int height);

	<T extends RenderElement> T withAlpha(float alpha);

	int getWidth();

	int getHeight();

	float getX();

	float getY();

	float getZ();

	void render(GuiGraphics graphics);

	@Override
	default void render(GuiGraphics graphics, int x, int y, float alpha) {
		this.at(x, y).withAlpha(alpha).render(graphics);
	}
}
