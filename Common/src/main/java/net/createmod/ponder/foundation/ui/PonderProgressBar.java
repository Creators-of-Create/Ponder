package net.createmod.ponder.foundation.ui;

import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.catnip.gui.UIRenderHelper;
import net.createmod.catnip.gui.element.BoxElement;
import net.createmod.catnip.gui.widget.AbstractSimiWidget;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.theme.Color;
import net.createmod.ponder.foundation.PonderScene;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;

public class PonderProgressBar extends AbstractSimiWidget {

	public static final Couple<Color> BAR_COLORS = Couple.create(
		new Color(0x80_aaaadd, true),
		new Color(0x50_aaaadd, true)
	).map(Color::setImmutable);

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
	public void setFocused(boolean focused) {
		super.setFocused(false);
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
	public void doRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		PoseStack poseStack = graphics.pose();

		isHovered = clicked(mouseX, mouseY);

		new BoxElement()
				.withBackground(PonderUI.BACKGROUND_FLAT)
				.gradientBorder(PonderUI.COLOR_IDLE)
				.at(getX(), getY(), 400)
				.withBounds(width, height)
				.render(graphics);

		poseStack.pushPose();
		poseStack.translate(getX() - 2, getY() - 2, 100);

		poseStack.pushPose();
		poseStack.scale((width + 4) * progress.getValue(partialTicks), 1, 1);
		Color c1 = BAR_COLORS.getFirst();
		Color c2 = BAR_COLORS.getSecond();
		UIRenderHelper.drawGradientRect(poseStack.last().pose(), 310, 0f, 1f, 1f, 3f, c1, c1);
		UIRenderHelper.drawGradientRect(poseStack.last().pose(), 310, 0f, 3f, 1f, 4f, c2, c2);
		poseStack.popPose();

		renderKeyframes(graphics, mouseX, partialTicks);

		poseStack.popPose();
	}

	private void renderKeyframes(GuiGraphics graphics, int mouseX, float partialTicks) {
		PonderScene activeScene = ponder.getActiveScene();

		Couple<Color> hover = PonderUI.COLOR_HOVER.map(c -> c.setAlpha(0xe0));
		Couple<Color> idle = PonderUI.COLOR_HOVER.map(c -> c.setAlpha(0x70));
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
			int keyframePos = (int) (((float) keyframeTime) / ((float) activeScene.getTotalTime()) * (width + 2));

			boolean selected = i == hoverIndex;
			Couple<Color> colors = selected ? hover : idle;
			int height = selected ? 8 : 4;

			drawKeyframe(graphics, activeScene, selected, keyframeTime, keyframePos, colors.getFirst(), colors.getSecond(), height);

		}
	}

	private void drawKeyframe(GuiGraphics graphics, PonderScene activeScene, boolean selected, int keyframeTime, int keyframePos, Color startColor, Color endColor, int height) {
		PoseStack poseStack = graphics.pose();
		if (selected) {
			Font font = Minecraft.getInstance().font;
			UIRenderHelper.drawGradientRect(poseStack.last().pose(), 320, ((float) keyframePos), 9f, keyframePos + 2f, 9f + height, endColor, startColor);
			poseStack.pushPose();
			poseStack.translate(0, 0, 320);
			String text;
			int offset;
			if (activeScene.getCurrentTime() < keyframeTime) {
				text = ">";
				offset = -2 - font.width(text);
			} else {
				text = "<";
				offset = 4;
			}
			graphics.drawString(font, Component.literal(text)
				.withStyle(ChatFormatting.BOLD), keyframePos + offset, 10, endColor.getRGB(), false);
			poseStack.popPose();
		}

		UIRenderHelper.drawGradientRect(poseStack.last().pose(), 320, ((float) keyframePos), 0f, keyframePos + 2f, 1f + height, startColor, endColor);
	}

	@Override
	public void playDownSound(SoundManager handler) {

	}
}
