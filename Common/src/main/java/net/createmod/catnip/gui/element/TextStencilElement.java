package net.createmod.catnip.gui.element;

import com.mojang.blaze3d.vertex.PoseStack;
import net.createmod.catnip.utility.lang.Components;
import net.createmod.catnip.utility.theme.Color;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.MutableComponent;

public class TextStencilElement extends DelegatedStencilElement {

	protected Font font;
	protected MutableComponent component = Components.empty();
	protected boolean centerVertically = false;
	protected boolean centerHorizontally = false;

	public TextStencilElement(Font font) {
		super();
		this.font = font;
		height = 10;
	}

	public TextStencilElement(Font font, String text) {
		this(font);
		component = Components.literal(text);
	}

	public TextStencilElement(Font font, MutableComponent component) {
		this(font);
		this.component = component;
	}

	public TextStencilElement withText(String text) {
		component = Components.literal(text);
		return this;
	}

	public TextStencilElement withText(MutableComponent component) {
		this.component = component;
		return this;
	}

	public TextStencilElement centered(boolean vertical, boolean horizontal) {
		this.centerVertically = vertical;
		this.centerHorizontally = horizontal;
		return this;
	}

	@Override
	public void renderStencil(GuiGraphics graphics) {

		float x = 0, y = 0;
		if (centerHorizontally)
			x = width / 2f - font.width(component) / 2f;

		if (centerVertically)
			y = height / 2f - (font.lineHeight - 1) / 2f;

		graphics.drawString(font, component, Math.round(x), Math.round(y), Color.BLACK.getRGB(), false);
		graphics.flush();
	}

	@Override
	public void renderElement(GuiGraphics graphics) {
		float x = 0, y = 0;
		if (centerHorizontally)
			x = width / 2f - font.width(component) / 2f;

		if (centerVertically)
			y = height / 2f - (font.lineHeight - 1) / 2f;

		PoseStack poseStack = graphics.pose();
		poseStack.pushPose();
		poseStack.translate(x, y, 0);
		element.render(graphics, font.width(component), font.lineHeight + 2, alpha);
		poseStack.popPose();
	}

	public MutableComponent getComponent() {
		return component;
	}
}
