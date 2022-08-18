package net.createmod.catnip.gui.element;

import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.catnip.utility.lang.Lang;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.MutableComponent;

public class TextStencilElement extends DelegatedStencilElement {

	protected Font font;
	protected MutableComponent component = Lang.empty().copy();
	protected boolean centerVertically = false;
	protected boolean centerHorizontally = false;

	public TextStencilElement(Font font) {
		super();
		this.font = font;
		height = 10;
	}

	public TextStencilElement(Font font, String text) {
		this(font);
		component = Lang.builder("catnip").text(text).component();
	}

	public TextStencilElement(Font font, MutableComponent component) {
		this(font);
		this.component = component;
	}

	public TextStencilElement withText(String text) {
		component = Lang.builder("catnip").text(text).component();
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
	public void renderStencil(PoseStack ms) {

		float x = 0, y = 0;
		if (centerHorizontally)
			x = width / 2f - font.width(component) / 2f;

		if (centerVertically)
			y = height / 2f - (font.lineHeight - 1) / 2f;

		font.draw(ms, component, x, y, 0xff_000000);
	}

	@Override
	public void renderElement(PoseStack ms) {
		float x = 0, y = 0;
		if (centerHorizontally)
			x = width / 2f - font.width(component) / 2f;

		if (centerVertically)
			y = height / 2f - (font.lineHeight - 1) / 2f;

		ms.pushPose();
		ms.translate(x, y, 0);
		element.render(ms, font.width(component), font.lineHeight + 2, alpha);
		ms.popPose();
	}

	public MutableComponent getComponent() {
		return component;
	}
}
