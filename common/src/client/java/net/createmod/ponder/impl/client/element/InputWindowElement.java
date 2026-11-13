package net.createmod.ponder.impl.client.element;

import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.Nullable;

import net.createmod.catnip.api.client.gui.element.GuiGameElement;
import net.createmod.catnip.api.client.gui.element.ScreenElement;
import net.createmod.catnip.api.client.gui.texture.CatnipGuiTextures;
import net.createmod.catnip.api.math.Pointing;
import net.createmod.ponder.api.Ponder;
import net.createmod.ponder.api.client.PonderIndex;
import net.createmod.ponder.api.client.PonderPalette;
import net.createmod.ponder.api.client.element.InputElementBuilder;
import net.createmod.ponder.api.client.scene.PonderScene;
import net.createmod.ponder.impl.client.gui.PonderUI;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class InputWindowElement extends AnimatedOverlayElementBase {
	private final Vec3 sceneSpace;
	private final Pointing direction;
	@Nullable
	Identifier key;
	@Nullable
	ScreenElement icon;
	ItemStack item = ItemStack.EMPTY;

	public InputWindowElement(Vec3 sceneSpace, Pointing direction) {
		this.sceneSpace = sceneSpace;
		this.direction = direction;
	}

	public InputElementBuilder builder() {
		return new Builder();
	}

	private class Builder implements InputElementBuilder {

		@Override
		public InputElementBuilder withItem(ItemStack stack) {
			item = stack;
			return this;
		}

		@Override
		public InputElementBuilder leftClick() {
			icon = CatnipGuiTextures.ICON_LMB;
			return this;
		}

		@Override
		public InputElementBuilder scroll() {
			icon = CatnipGuiTextures.ICON_SCROLL;
			return this;
		}

		@Override
		public InputElementBuilder rightClick() {
			icon = CatnipGuiTextures.ICON_RMB;
			return this;
		}

		@Override
		public InputElementBuilder showing(ScreenElement icon) {
			InputWindowElement.this.icon = icon;
			return this;
		}

		@Override
		public InputElementBuilder whileSneaking() {
			key = Ponder.id("sneak_and");
			return this;
		}

		@Override
		public InputElementBuilder whileCTRL() {
			key = Ponder.id("ctrl_and");
			return this;
		}

	}

	@Override
	public void render(PonderScene scene, PonderUI screen, GuiGraphicsExtractor graphics, float partialTicks, float fade) {
		Font font = screen.getFont();
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
		String text = hasText ? PonderIndex.getLangAccess().getShared(key) : "";

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

		Matrix3x2fStack poseStack = graphics.pose();
		poseStack.pushMatrix();
		poseStack.translate(sceneToScreen.x + xFade, sceneToScreen.y + yFade);

		PonderUI.renderSpeechBox(graphics, 0, 0, width, height, false, direction, true);

		if (hasText)
			graphics.text(font, text, 2, (int) ((height - font.lineHeight) / 2f + 2),
				PonderPalette.WHITE.getColorObject().scaleAlpha(fade).getRGB(), false);

		if (hasIcon) {
			poseStack.pushMatrix();
			poseStack.translate(keyWidth, 0);
			poseStack.scale(1.5f, 1.5f);
			icon.render(graphics, 0, 0);
			poseStack.popMatrix();
		}

		if (hasItem) {
			GuiGameElement.of(item)
				.<GuiGameElement.GuiRenderBuilder>at(keyWidth + (hasIcon ? 24 : 0), 0)
				.scale(1.5)
				.submit(graphics);
		}

		poseStack.popMatrix();
	}
}
