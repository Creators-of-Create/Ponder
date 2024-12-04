package net.createmod.catnip.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.catnip.enums.CatnipGuiTextures;
import net.createmod.catnip.gui.element.BoxElement;
import net.createmod.catnip.gui.widget.BoxWidget;
import net.createmod.catnip.utility.Couple;
import net.createmod.catnip.utility.animation.LerpedFloat;
import net.createmod.catnip.utility.lang.Lang;
import net.createmod.catnip.utility.theme.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public abstract class NavigatableSimiScreen extends AbstractSimiScreen {

	public static final Couple<Color> COLOR_NAV_ARROW = Couple.create(
		new Color(0x80_aa9999, true),
		new Color(0x30_aa9999)
	).map(Color::setImmutable);

	protected static boolean currentlyRenderingPreviousScreen = false;

	protected int depthPointX, depthPointY;
	public final LerpedFloat transition = LerpedFloat.linear()
		.startWithValue(0)
		.chase(0, .1f, LerpedFloat.Chaser.LINEAR);
	protected final LerpedFloat arrowAnimation = LerpedFloat.linear()
		.startWithValue(0)
		.chase(0, 0.075f, LerpedFloat.Chaser.LINEAR);
	@Nullable protected BoxWidget backTrack;

	public NavigatableSimiScreen() {
		Window window = Minecraft.getInstance().getWindow();
		depthPointX = window.getGuiScaledWidth() / 2;
		depthPointY = window.getGuiScaledHeight() / 2;
	}

	@Override
	public void onClose() {
		ScreenOpener.clearStack();
		super.onClose();
	}

	@Override
	public void tick() {
		super.tick();
		transition.tickChaser();
		arrowAnimation.tickChaser();
	}

	@Override
	protected void init() {
		super.init();

		backTrack = null;
		List<Screen> screenHistory = ScreenOpener.getScreenHistory();
		if (screenHistory.isEmpty())
			return;

		addRenderableWidget(backTrack = new BoxWidget(31, height - 31 - 20)
				.withBounds(20, 20)
				.withCustomBackground(BoxElement.COLOR_BACKGROUND_FLAT)
				.enableFade(0, 5)
				.withPadding(2, 2)
				.fade(1)
				.withCallback(() -> ScreenOpener.openPreviousScreen(this, null)));

		Screen previousScreen = screenHistory.get(0);
		if (previousScreen instanceof NavigatableSimiScreen screen) {
			screen.initBackTrackIcon(backTrack);
		} else {
			backTrack.showing(CatnipGuiTextures.ICON_DISABLE);
		}

	}

	/**
	 * Called when {@code this} represents the previous screen to
	 * initialize the {@code backTrack} icon of the current screen.
	 *
	 * @param backTrack The backTrack button of the current screen.
	 */
	protected abstract void initBackTrackIcon(BoxWidget backTrack);

	protected Component backTrackingComponent() {
		if (ScreenOpener.getBackStepScreen() instanceof NavigatableSimiScreen) {
			return Lang.builder("catnip")
					.translate("gui.step_back")
					.component();
		}

		return Lang.builder("catnip")
				.translate("gui.exit")
				.component();
	}

	@Override
	protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
//		renderZeloBreadcrumbs(ms, mouseX, mouseY, partialTicks);
		if (backTrack == null)
			return;

		PoseStack poseStack = graphics.pose();

		int x = (int) Mth.lerp(arrowAnimation.getValue(partialTicks), -9, 21);
		int maxX = backTrack.getX() + backTrack.getWidth();
		Couple<Color> colors = COLOR_NAV_ARROW;

		poseStack.pushPose();
		poseStack.translate(0, 0, -300);
		if (x + 30 < backTrack.getX())
			UIRenderHelper.breadcrumbArrow(graphics, x + 30, height - 51, 0, maxX - (x + 30), 20, 5, colors);

		UIRenderHelper.breadcrumbArrow(graphics, x, height - 51, 0, 30, 20, 5, colors);
		UIRenderHelper.breadcrumbArrow(graphics, x - 30, height - 51, 0, 30, 20, 5, colors);
		poseStack.popPose();

		poseStack.pushPose();
		poseStack.translate(0, 0, 500);
		if (backTrack.isHoveredOrFocused()) {
			Component component = backTrackingComponent();
			graphics.drawString(font, component, 41 - font.width(component) / 2, height - 16, UIRenderHelper.COLOR_TEXT_DARKER.getFirst().getRGB(), false);
			if (Mth.equal(arrowAnimation.getValue(), arrowAnimation.getChaseTarget())) {
				arrowAnimation.setValue(1);
				arrowAnimation.setValue(1);// called twice to also set the previous value to 1
			}
		}
		poseStack.popPose();
	}

	@Override
	public void renderBackground(GuiGraphics graphics) {
		if (!isCurrentlyRenderingPreviousScreen())
			super.renderBackground(graphics);
	}

	@Override
	protected void renderWindowBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		if (transition.getChaseTarget() == 0 || transition.settled()) {
			renderBackground(graphics);
			return;
		}

		renderBackground(graphics);

		PoseStack ms = graphics.pose();

		Window window = minecraft.getWindow();
		float guiScaledWidth = window.getGuiScaledWidth();
		float guiScaledHeight = window.getGuiScaledHeight();

		Screen lastScreen = ScreenOpener.getPreviouslyRenderedScreen();
		float tValue = transition.getValue(partialTicks);
		float tValueAbsolute = Math.abs(tValue);

		// draw last screen into buffer
		if (lastScreen != null && lastScreen != this && !transition.settled()) {
			currentlyRenderingPreviousScreen = true;
			ms.pushPose();
			UIRenderHelper.framebuffer.clear(Minecraft.ON_OSX);
			UIRenderHelper.framebuffer.bindWrite(true);
			lastScreen.render(graphics, 0, 0, partialTicks);

			ms.popPose();

			ms.pushPose();
			minecraft.getMainRenderTarget().bindWrite(true);

			int dpx = (int) (guiScaledWidth / 2);
			int dpy = (int) (guiScaledHeight / 2);
			if (lastScreen instanceof NavigatableSimiScreen navigableScreen && tValue > 0) {
				dpx = navigableScreen.depthPointX;
				dpy = navigableScreen.depthPointY;
			}

			float scale = 1 + (0.2f * tValue);

			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			Matrix4f matrix4f = new Matrix4f().setOrtho(0.0F, guiScaledWidth, guiScaledHeight, 0.0F, 1000.0F, 3000.0F);
			PoseStack poseStack2 = new PoseStack();
			poseStack2.last().pose().set(matrix4f);
			poseStack2.translate(dpx, dpy, 0);
			poseStack2.scale(scale, scale, 1);
			poseStack2.translate(-dpx, -dpy, 0);


			UIRenderHelper.drawFramebuffer(poseStack2, 1f - tValueAbsolute);
			RenderSystem.disableBlend();
			ms.popPose();
			currentlyRenderingPreviousScreen = false;
		}

		// modify current screen as well
		float scale = tValue > 0 ? 1 - 0.5f * (1 - tValueAbsolute) : 1 + .5f * (1 - tValueAbsolute);
		int dpx = (int) (guiScaledWidth / 2);
		//dpx = depthPointX;
		int dpy = (int) (guiScaledHeight / 2);
		//dpy = depthPointY;
		ms.translate(dpx, dpy, 0);
		ms.scale(scale, scale, 1);
		ms.translate(-dpx, -dpy, 0);
	}

	@Override
	public boolean keyPressed(int code, int p_keyPressed_2_, int p_keyPressed_3_) {
		if (code == GLFW.GLFW_KEY_BACKSPACE) {
			ScreenOpener.openPreviousScreen(this, null);
			return true;
		}
		return super.keyPressed(code, p_keyPressed_2_, p_keyPressed_3_);
	}

	public void centerScalingOn(int x, int y) {
		depthPointX = x;
		depthPointY = y;
	}

	public void centerScalingOnMouse() {
		Window w = minecraft.getWindow();
		double mouseX = minecraft.mouseHandler.xpos() * w.getGuiScaledWidth() / w.getScreenWidth();
		double mouseY = minecraft.mouseHandler.ypos() * w.getGuiScaledHeight() / w.getScreenHeight();
		centerScalingOn((int) mouseX, (int) mouseY);
	}

	public boolean isEquivalentTo(NavigatableSimiScreen other) {
		return false;
	}

	public void shareContextWith(NavigatableSimiScreen other) {}

	protected void renderZeloBreadcrumbs(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		List<Screen> history = ScreenOpener.getScreenHistory();
		if (history.isEmpty())
			return;

		history.add(0, minecraft.screen);
		int spacing = 20;

		List<String> names = new ArrayList<>();
		for (Screen screen : history)
			names.add(NavigatableSimiScreen.screenTitle(screen));

		int bWidth = 0;
		for (String name : names) {
			bWidth += font.width(name) + spacing;
		}

		MutableInt x = new MutableInt(width - bWidth);
		MutableInt y = new MutableInt(height - 18);
		MutableBoolean first = new MutableBoolean(true);

		if (x.getValue() < 25)
			x.setValue(25);

		PoseStack poseStack = graphics.pose();
		poseStack.pushPose();
		poseStack.translate(0, 0, 600);
		names.forEach(s -> {
			int sWidth = font.width(s);
			UIRenderHelper.breadcrumbArrow(graphics, x.getValue(), y.getValue(), 0, sWidth + spacing, 14, spacing / 2,
					new Color(0xdd101010), new Color(0x44101010));
			graphics.drawString(font, s, x.getValue() + 5, y.getValue() + 3, first.getValue() ? 0xffeeffee : 0xffddeeff);
			first.setFalse();

			x.add(sWidth + spacing);
		});
		poseStack.popPose();
	}

	public static boolean isCurrentlyRenderingPreviousScreen() {
		return currentlyRenderingPreviousScreen;
	}

	private static String screenTitle(Screen screen) {
		if (screen instanceof NavigatableSimiScreen)
			return ((NavigatableSimiScreen) screen).getBreadcrumbTitle();
		return "<";
	}

	protected String getBreadcrumbTitle() {
		return this.getClass()
			.getSimpleName();
	}
}
