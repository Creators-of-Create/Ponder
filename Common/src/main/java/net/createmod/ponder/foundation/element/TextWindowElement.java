package net.createmod.ponder.foundation.element;

import java.util.List;
import java.util.function.Supplier;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;

import net.createmod.catnip.gui.UIRenderHelper;
import net.createmod.catnip.gui.element.BoxElement;
import net.createmod.catnip.utility.theme.Color;
import net.createmod.catnip.utility.theme.Theme;
import net.createmod.ponder.foundation.PonderLocalization;
import net.createmod.ponder.foundation.PonderPalette;
import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.ui.PonderUI;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class TextWindowElement extends AnimatedOverlayElement {

	Supplier<String> textGetter = () -> "(?) No text was provided";
	String bakedText;

	// from 0 to 200
	int y;

	Vec3 vec;

	boolean nearScene = false;
	PonderPalette palette = PonderPalette.WHITE;

	public class Builder {

		private PonderScene scene;

		public Builder(PonderScene scene) {
			this.scene = scene;
		}

		public Builder colored(PonderPalette color) {
			TextWindowElement.this.palette = color;
			return this;
		}

		public Builder pointAt(Vec3 vec) {
			TextWindowElement.this.vec = vec;
			return this;
		}

		public Builder independent(int y) {
			TextWindowElement.this.y = y;
			return this;
		}

		public Builder independent() {
			return independent(0);
		}

		public Builder text(String defaultText) {
			textGetter = scene.registerText(defaultText);
			return this;
		}

		public Builder sharedText(ResourceLocation key) {
			textGetter = () -> PonderLocalization.getShared(key);
			return this;
		}

		public Builder sharedText(String key) {
			return sharedText(new ResourceLocation(scene.getNamespace(), key));
		}

		public Builder placeNearTarget() {
			TextWindowElement.this.nearScene = true;
			return this;
		}

		public Builder attachKeyFrame() {
			scene.builder()
				.addLazyKeyframe();
			return this;
		}

	}

	@Override
	protected void render(PonderScene scene, PonderUI screen, PoseStack ms, float partialTicks, float fade) {
		if (bakedText == null)
			bakedText = textGetter.get();
		if (fade < 1 / 16f)
			return;
		Vec2 sceneToScreen = vec != null ? scene.getTransform()
			.sceneToScreen(vec, partialTicks) : new Vec2(screen.width / 2, (screen.height - 200) / 2 + y - 8);

		float yDiff = (screen.height / 2f - sceneToScreen.y - 10) / 100f;
		int targetX = (int) (screen.width * Mth.lerp(yDiff * yDiff, 6f / 8, 5f / 8));

		if (nearScene)
			targetX = (int) Math.min(targetX, sceneToScreen.x + 50);

		int textWidth = Math.min(screen.width - targetX, 180);

		List<FormattedText> lines = screen.getFontRenderer().getSplitter().splitLines(bakedText, textWidth, Style.EMPTY);

		int boxWidth = 0;
		for (FormattedText line : lines)
			boxWidth = Math.max(boxWidth, screen.getFontRenderer().width(line));

		int boxHeight = screen.getFontRenderer()
			.wordWrapHeight(bakedText, boxWidth);

		ms.pushPose();
		ms.translate(0, sceneToScreen.y, 400);

		new BoxElement()
				.withBackground(Theme.Key.PONDER_BACKGROUND_FLAT.c())
				.gradientBorder(Theme.Key.TEXT_WINDOW_BORDER.p())
				.at(targetX - 10, 3, 100)
				.withBounds(boxWidth, boxHeight - 1)
				.render(ms);

		//PonderUI.renderBox(ms, targetX - 10, 3, boxWidth, boxHeight - 1, 0xaa000000, 0x30eebb00, 0x10eebb00);

		Color brighter = palette.getColorObject().mixWith(new Color(0xff_ffffdd), 0.5f);
		Color c1 = new Color(0xff_494949);
		Color c2 = new Color(0xff_393939);
		if (vec != null) {
			ms.pushPose();
			ms.translate(sceneToScreen.x, 0, 0);
			double lineTarget = (targetX - sceneToScreen.x) * fade;
			ms.scale((float) lineTarget, 1, 1);
			Matrix4f model = ms.last().pose();
			UIRenderHelper.drawGradientRect(model, -100, 0, 0, 1, 1, brighter, brighter);
			UIRenderHelper.drawGradientRect(model, -100, 0, 1, 1, 2, c1, c2);
			ms.popPose();
		}

		ms.translate(0, 0, 400);
		for (int i = 0; i < lines.size(); i++) {
			screen.getFontRenderer()
				.draw(ms, lines.get(i).getString(), targetX - 10, 3 + 9 * i, brighter.scaleAlpha(fade).getRGB());
		}
		ms.popPose();
	}

	public PonderPalette getPalette() {
		return palette;
	}

}
