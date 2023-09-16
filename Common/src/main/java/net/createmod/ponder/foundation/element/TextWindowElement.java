package net.createmod.ponder.foundation.element;

import com.mojang.blaze3d.vertex.PoseStack;
import net.createmod.catnip.gui.element.BoxElement;
import net.createmod.catnip.utility.theme.Color;
import net.createmod.ponder.foundation.PonderIndex;
import net.createmod.ponder.foundation.PonderPalette;
import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.PonderScene.SceneTransform;
import net.createmod.ponder.foundation.PonderTheme;
import net.createmod.ponder.foundation.ui.PonderUI;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Supplier;

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
			textGetter = () -> PonderIndex.getLangAccess().getShared(key);
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
	protected void render(PonderScene scene, PonderUI screen, GuiGraphics graphics, float partialTicks, float fade) {
		if (bakedText == null)
			bakedText = textGetter.get();
		if (fade < 1 / 16f)
			return;
		SceneTransform transform = scene.getTransform();
		Vec2 sceneToScreen = vec != null ? transform.sceneToScreen(vec, partialTicks)
				: new Vec2(screen.width / 2, (screen.height - 200) / 2 + y - 8);

		boolean settled = transform.xRotation.settled() && transform.yRotation.settled();
		float pY = settled ? (int) sceneToScreen.y : sceneToScreen.y;

		float yDiff = (screen.height / 2f - sceneToScreen.y - 10) / 100f;
		float targetX = (screen.width * Mth.lerp(yDiff * yDiff, 6f / 8, 5f / 8));

		if (nearScene)
			targetX = Math.min(targetX, sceneToScreen.x + 50);

		if (settled)
			targetX = (int) targetX;

		int textWidth = (int) Math.min(screen.width - targetX, 180);

		List<FormattedText> lines = screen.getFontRenderer()
				.getSplitter()
				.splitLines(bakedText, textWidth, Style.EMPTY);

		int boxWidth = 0;
		for (FormattedText line : lines)
			boxWidth = Math.max(boxWidth, screen.getFontRenderer()
					.width(line));

		int boxHeight = screen.getFontRenderer()
				.wordWrapHeight(bakedText, boxWidth);

		PoseStack poseStack = graphics.pose();
		poseStack.pushPose();
		poseStack.translate(0, pY, 400);

		new BoxElement()
				.withBackground(PonderTheme.Key.PONDER_BACKGROUND_FLAT.c())
				.gradientBorder(PonderTheme.Key.TEXT_WINDOW_BORDER.p())
				.at(targetX - 10, 3, 100)
				.withBounds(boxWidth, boxHeight - 1)
				.render(graphics);

		Color brighter = palette.getColorObject().mixWith(new Color(0xff_ffffdd), 0.5f).setImmutable();
		Color c1 = new Color(0xff_494949);
		Color c2 = new Color(0xff_393939);
		if (vec != null) {
			poseStack.pushPose();
			poseStack.translate(sceneToScreen.x, 0, 0);
			double lineTarget = (targetX - sceneToScreen.x) * fade;
			poseStack.scale((float) lineTarget, 1, 1);
			graphics.fillGradient(0, 0, 1, 1, -100, brighter.getRGB(), brighter.getRGB());
			graphics.fillGradient(0, 1, 1, 2, -100, c1.getRGB(), c2.getRGB());
			poseStack.popPose();
		}

		poseStack.translate(0, 0, 400);
		for (int i = 0; i < lines.size(); i++) {
			graphics.drawString(screen.getFontRenderer(), lines.get(i).getString(), (int) (targetX - 10), 3 + 9 * i, brighter.scaleAlphaForText(fade).getRGB(), false);
		}
		poseStack.popPose();
	}

	public PonderPalette getPalette() {
		return palette;
	}

}
