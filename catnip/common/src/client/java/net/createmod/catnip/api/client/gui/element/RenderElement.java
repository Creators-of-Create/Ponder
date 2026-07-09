package net.createmod.catnip.api.client.gui.element;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public interface RenderElement extends FadableScreenElement {
	static RenderElement of(ScreenElement renderable) {
		return new AbstractRenderElement.SimpleRenderElement(renderable);
	}

	<T extends RenderElement> T at(float x, float y);

	// TODO - Remove this if z is unneeded
	<T extends RenderElement> T at(float x, float y, float z);// GuiGraphics automatically sorts render order (PIP rendering should be used if z is stilled needed)

	<T extends RenderElement> T withBounds(int width, int height);

	<T extends RenderElement> T withAlpha(float alpha);

	int getWidth();

	int getHeight();

	float getX();

	float getY();

	void submit(GuiGraphicsExtractor graphics);

	default void render(GuiGraphicsExtractor graphics) {
		render(graphics, 0, 0);
	}

	@Override
	default void render(GuiGraphicsExtractor graphics, int x, int y, float alpha) {
		this.at(x, y).withAlpha(alpha).submit(graphics);
	}
}
