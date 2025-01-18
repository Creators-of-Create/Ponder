package net.createmod.catnip.config.ui;

import org.lwjgl.glfw.GLFW;

import net.createmod.catnip.gui.UIRenderHelper;
import net.createmod.catnip.lang.Components;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;

public class HintableTextFieldWidget extends EditBox {

	protected Font font;
	protected String hint;

	public HintableTextFieldWidget(Font font, int x, int y, int width, int height) {
		super(font, x, y, width, height, Components.immutableEmpty());
		this.font = font;
	}

	public void setHint(String hint) {
		this.hint = hint;
	}

	public void setHeight(int value) {
		this.height = value;
	}

	@Override
	public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		super.renderWidget(graphics, mouseX, mouseY, partialTicks);

		if (hint == null || hint.isEmpty())
			return;

		if (!getValue().isEmpty())
			return;

		graphics.drawString(font, hint, getX() + 5, this.getY() + (this.height - 8) / 2, UIRenderHelper.COLOR_TEXT.getFirst().scaleAlpha(.75f).getRGB());
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {
		if (!isMouseOver(x, y))
			return false;

		if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
			setValue("");
			return true;
		} else
			return super.mouseClicked(x, y, button);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (Minecraft.getInstance().options.keyInventory.matches(keyCode, scanCode)) {
			return true;
		}

		return super.keyPressed(keyCode, scanCode, modifiers);
	}
}
