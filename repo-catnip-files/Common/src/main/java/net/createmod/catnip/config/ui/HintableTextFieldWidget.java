package net.createmod.catnip.config.ui;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.catnip.utility.theme.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.TextComponent;

public class HintableTextFieldWidget extends EditBox {

	protected Font font;
	protected String hint;

	public HintableTextFieldWidget(Font font, int x, int y, int width, int height) {
		super(font, x, y, width, height, TextComponent.EMPTY);
		this.font = font;
	}

	public void setHint(String hint) {
		this.hint = hint;
	}

	public void setHeight(int value) {
		this.height = value;
	}

	@Override
	public void renderButton(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
		super.renderButton(ms, mouseX, mouseY, partialTicks);

		if (hint == null || hint.isEmpty())
			return;

		if (!getValue().isEmpty())
			return;

		font.draw(ms, hint, x + 5, this.y + (this.height - 8) / 2, Theme.Key.TEXT.c().scaleAlpha(.75f).getRGB());
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
