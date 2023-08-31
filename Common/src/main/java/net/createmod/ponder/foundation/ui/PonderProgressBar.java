package net.createmod.ponder.foundation.ui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.createmod.catnip.gui.UIRenderHelper;
import net.createmod.catnip.gui.element.BoxElement;
import net.createmod.catnip.gui.widget.AbstractSimiWidget;
import net.createmod.catnip.utility.Couple;
import net.createmod.catnip.utility.animation.LerpedFloat;
import net.createmod.catnip.utility.theme.Color;
import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.PonderTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.sounds.SoundManager;

public class PonderProgressBar extends AbstractSimiWidget {

	LerpedFloat progress;

	PonderUI ponder;

	public PonderProgressBar(PonderUI ponder, int xIn, int yIn, int widthIn, int heightIn) {
		super(xIn, yIn, widthIn, heightIn);

		this.ponder = ponder;
		progress = LerpedFloat.linear()
				.startWithValue(0);
	}

	public void tick() {
		progress.chase(ponder.getActiveScene()
				.getSceneProgress(), .5f, LerpedFloat.Chaser.EXP);
		progress.tickChaser();
	}

	@Override
	protected boolean clicked(double mouseX, double mouseY) {
		return this.active && this.visible && ponder.getActiveScene().getKeyframeCount() > 0
				&& mouseX >= (double) this.getX() && mouseX < (double) (this.getX() + this.width + 4) && mouseY >= (double) this.getY() - 3
				&& mouseY < (double) (this.getY() + this.height + 20);
	}

	@Override
	public void onClick(double mouseX, double mouseY) {
		PonderScene activeScene = ponder.getActiveScene();

		int keyframeIndex = getHoveredKeyframeIndex(activeScene, mouseX);

		if (keyframeIndex == -1)
			ponder.seekToTime(0);
		else if (keyframeIndex == activeScene.getKeyframeCount())
			ponder.seekToTime(activeScene.getTotalTime());
		else
			ponder.seekToTime(activeScene.getKeyframeTime(keyframeIndex));
	}

	public int getHoveredKeyframeIndex(PonderScene activeScene, double mouseX) {
		int totalTime = activeScene.getTotalTime();
		int clickedAtTime = (int) ((mouseX - getX()) / ((double) width + 4) * totalTime);

		{
			int lastKeyframeTime = activeScene.getKeyframeTime(activeScene.getKeyframeCount() - 1);

			int diffToEnd = totalTime - clickedAtTime;
			int diffToLast = clickedAtTime - lastKeyframeTime;

			if (diffToEnd > 0 && diffToEnd < diffToLast / 2) {
				return activeScene.getKeyframeCount();
			}
		}

		int index = -1;

		for (int i = 0; i < activeScene.getKeyframeCount(); i++) {
			int keyframeTime = activeScene.getKeyframeTime(i);

			if (keyframeTime > clickedAtTime)
				break;

			index = i;
		}

		return index;
	}

	@Override
	public void renderButton(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		PoseStack poseStack = graphics.pose();

		isHovered = clicked(mouseX, mouseY);

		new BoxElement()
				.withBackground(PonderTheme.Key.PONDER_BACKGROUND_FLAT.c())
				.gradientBorder(PonderTheme.Key.PONDER_IDLE.p())
				.at(getX(), getY(), 400)
				.withBounds(width, height)
				.render(graphics);

		poseStack.pushPose();
		poseStack.translate(getX() - 2, getY() - 2, 100);

		poseStack.pushPose();
		poseStack.scale((width + 4) * progress.getValue(partialTicks), 1, 1);
		Color c1 = PonderTheme.Key.PONDER_PROGRESSBAR.c(true);
		Color c2 = PonderTheme.Key.PONDER_PROGRESSBAR.c(false);
		UIRenderHelper.drawGradientRect(poseStack.last().pose(), 310, 0, 3, 1, 4, c1, c1);
		UIRenderHelper.drawGradientRect(poseStack.last().pose(), 310, 0, 4, 1, 5, c2, c2);
		poseStack.popPose();

		renderKeyframes(graphics, mouseX, partialTicks);

		poseStack.popPose();
	}

	private void renderKeyframes(GuiGraphics graphics, int mouseX, float partialTicks) {
		PonderScene activeScene = ponder.getActiveScene();

		Couple<Color> hover = PonderTheme.Key.PONDER_HOVER.p().map(c -> c.setAlpha(0xa0));
		Couple<Color> idle = PonderTheme.Key.PONDER_HOVER.p().map(c -> c.setAlpha(0x40));
		int hoverStartColor = PonderTheme.Key.PONDER_HOVER.i(true) | 0xa0_000000;
		int hoverEndColor = PonderTheme.Key.PONDER_HOVER.i(false) | 0xa0_000000;
		int idleStartColor = PonderTheme.Key.PONDER_IDLE.i(true) | 0x40_000000;
		int idleEndColor = PonderTheme.Key.PONDER_IDLE.i(false) | 0x40_000000;
		int hoverIndex;

		if (isHovered) {
			hoverIndex = getHoveredKeyframeIndex(activeScene, mouseX);
		} else {
			hoverIndex = -2;
		}

		if (hoverIndex == -1)
			drawKeyframe(graphics, activeScene, true, 0, 0, hover.getFirst(), hover.getSecond(), 8);
		else if (hoverIndex == activeScene.getKeyframeCount())
			drawKeyframe(graphics, activeScene, true, activeScene.getTotalTime(), width + 4, hover.getFirst(), hover.getSecond(), 8);

		for (int i = 0; i < activeScene.getKeyframeCount(); i++) {
			int keyframeTime = activeScene.getKeyframeTime(i);
			int keyframePos = (int) (((float) keyframeTime) / ((float) activeScene.getTotalTime()) * (width + 4));

			boolean selected = i == hoverIndex;
			Couple<Color> colors = selected ? hover : idle;
			int startColor = selected ? hoverStartColor : idleStartColor;
			int endColor = selected ? hoverEndColor : idleEndColor;
			int height = selected ? 8 : 4;

			drawKeyframe(graphics, activeScene, selected, keyframeTime, keyframePos, colors.getFirst(), colors.getSecond(), height);

		}
	}

	private void drawKeyframe(GuiGraphics graphics, PonderScene activeScene, boolean selected, int keyframeTime, int keyframePos, Color startColor, Color endColor, int height) {
		PoseStack poseStack = graphics.pose();
		if (selected) {
			Font font = Minecraft.getInstance().font;
			UIRenderHelper.drawGradientRect(poseStack.last().pose(), 600, keyframePos, 10, keyframePos + 1, 10 + height, endColor, startColor);
			poseStack.pushPose();
			poseStack.translate(0, 0, 200);
			String text;
			int offset;
			if (activeScene.getCurrentTime() < keyframeTime) {
				text = ">";
				offset = -1 - font.width(text);
			} else {
				text = "<";
				offset = 3;
			}
			graphics.drawString(font, text, keyframePos + offset, 10, endColor.getRGB(), false);
			poseStack.popPose();
		}

		UIRenderHelper.drawGradientRect(poseStack.last().pose(), 400, keyframePos, -1, keyframePos + 1, 2 + height, startColor, endColor);
	}

	@Override
	public void playDownSound(SoundManager handler) {

	}
}
