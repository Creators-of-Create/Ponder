package net.createmod.ponder.foundation.element;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.createmod.catnip.gui.element.ScreenElement;
import net.createmod.catnip.utility.Pointing;
import net.createmod.ponder.Ponder;
import net.createmod.ponder.enums.PonderGuiTextures;
import net.createmod.ponder.foundation.PonderLocalization;
import net.createmod.ponder.foundation.PonderPalette;
import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.ui.PonderUI;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class InputWindowElement extends AnimatedOverlayElement {

	private Pointing direction;
	ResourceLocation key;
	ScreenElement icon;
	ItemStack item = ItemStack.EMPTY;
	private Vec3 sceneSpace;

	public InputWindowElement clone() {
		InputWindowElement inputWindowElement = new InputWindowElement(sceneSpace, direction);
		inputWindowElement.key = key;
		inputWindowElement.icon = icon;
		inputWindowElement.item = item.copy();
		return inputWindowElement;
	}

	public InputWindowElement(Vec3 sceneSpace, Pointing direction) {
		this.sceneSpace = sceneSpace;
		this.direction = direction;
	}

	public InputWindowElement withItem(ItemStack stack) {
		item = stack;
		return this;
	}

	public InputWindowElement leftClick() {
		icon = PonderGuiTextures.ICON_LMB;
		return this;
	}

	public InputWindowElement scroll() {
		icon = PonderGuiTextures.ICON_SCROLL;
		return this;
	}

	public InputWindowElement rightClick() {
		icon = PonderGuiTextures.ICON_RMB;
		return this;
	}

	public InputWindowElement showing(ScreenElement icon) {
		this.icon = icon;
		return this;
	}

	public InputWindowElement whileSneaking() {
		key = Ponder.asResource("sneak_and");
		return this;
	}

	public InputWindowElement whileCTRL() {
		key = Ponder.asResource("ctrl_and");
		return this;
	}

	@Override
	protected void render(PonderScene scene, PonderUI screen, GuiGraphics graphics, float partialTicks, float fade) {
		Font font = screen.getFontRenderer();
		int width = 0;
		int height = 0;

		float xFade = direction == Pointing.RIGHT ? -1 : direction == Pointing.LEFT ? 1 : 0;
		float yFade = direction == Pointing.DOWN ? -1 : direction == Pointing.UP ? 1 : 0;
		xFade *= 10 * (1 - fade);
		yFade *= 10 * (1 - fade);

		boolean hasItem = !item.isEmpty();
		boolean hasText = key != null;
		boolean hasIcon = icon != null;
		int keyWidth = 0;
		String text = hasText ? PonderLocalization.getShared(key) : "";

		if (fade < 1 / 16f)
			return;
		Vec2 sceneToScreen = scene.getTransform()
			.sceneToScreen(sceneSpace, partialTicks);

		if (hasIcon) {
			width += 24;
			height = 24;
		}

		if (hasText) {
			keyWidth = font.width(text);
			width += keyWidth;
		}

		if (hasItem) {
			width += 24;
			height = 24;
		}

		PoseStack poseStack = graphics.pose();
		poseStack.pushPose();
		poseStack.translate(sceneToScreen.x + xFade, sceneToScreen.y + yFade, 400);

		PonderUI.renderSpeechBox(graphics, 0, 0, width, height, false, direction, true);

		poseStack.translate(0, 0, 100);

		if (hasText)
			graphics.drawString(font, text, 2, (int) ((height - font.lineHeight) / 2f + 2),
								PonderPalette.WHITE.getColorObject().scaleAlpha(fade).getRGB(), false);

		if (hasIcon) {
			poseStack.pushPose();
			poseStack.translate(keyWidth, 0, 0);
			poseStack.scale(1.5f, 1.5f, 1.5f);
			icon.render(graphics, 0, 0);
			poseStack.popPose();
		}

		if (hasItem) {
			GuiGameElement.of(item)
				.<GuiGameElement.GuiRenderBuilder>at(keyWidth + (hasIcon ? 24 : 0), 0)
				.scale(1.5)
				.render(graphics);
			RenderSystem.disableDepthTest();
		}

		poseStack.popPose();
	}

}
