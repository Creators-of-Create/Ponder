package net.createmod.catnip.gui;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;
import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;

import net.createmod.catnip.enums.CatnipGuiTextures;
import net.createmod.catnip.gui.widget.BoxWidget;
import net.createmod.catnip.utility.Couple;
import net.createmod.catnip.utility.animation.LerpedFloat;
import net.createmod.catnip.utility.lang.Lang;
import net.createmod.catnip.utility.theme.Color;
import net.createmod.catnip.utility.theme.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public abstract class NavigatableSimiScreen extends AbstractSimiScreen {

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
				.withCustomBackground(Theme.Key.BOX_BACKGROUND_FLAT.c())
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
	protected void renderWindow(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
//		renderZeloBreadcrumbs(ms, mouseX, mouseY, partialTicks);
		if (backTrack == null)
			return;

		int x = (int) Mth.lerp(arrowAnimation.getValue(partialTicks), -9, 21);
		int maxX = backTrack.x + backTrack.getWidth();
		Couple<Color> colors = Theme.Key.NAV_BACK_ARROW.p();

		ms.pushPose();
		ms.translate(0, 0, -300);
		if (x + 30 < backTrack.x)
			UIRenderHelper.breadcrumbArrow(ms, x + 30, height - 51, 0, maxX - (x + 30), 20, 5, colors);

		UIRenderHelper.breadcrumbArrow(ms, x, height - 51, 0, 30, 20, 5, colors);
		UIRenderHelper.breadcrumbArrow(ms, x - 30, height - 51, 0, 30, 20, 5, colors);
		ms.popPose();

		ms.pushPose();
		ms.translate(0, 0, 500);
		if (backTrack.isHoveredOrFocused()) {
			Component component = backTrackingComponent();
			font.draw(ms, component, 41 - font.width(component) / 2, height - 16, Theme.Key.TEXT_DARKER.i());
			if (Mth.equal(arrowAnimation.getValue(), arrowAnimation.getChaseTarget())) {
				arrowAnimation.setValue(1);
				arrowAnimation.setValue(1);// called twice to also set the previous value to 1
			}
		}
		ms.popPose();
	}

	@Override
	public void renderBackground(PoseStack $$0) {
		if (!isCurrentlyRenderingPreviousScreen())
			super.renderBackground($$0);
	}

	@Override
	protected void renderWindowBackground(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
		if (transition.getChaseTarget() == 0 || transition.settled()) {
			renderBackground(ms);
			return;
		}

		renderBackground(ms);

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
			lastScreen.render(ms, mouseX, mouseY, 0);
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
			Matrix4f matrix4f = Matrix4f.orthographic(guiScaledWidth, -guiScaledHeight, 1000.0F, 3000.0F);
			PoseStack poseStack2 = new PoseStack();
			poseStack2.last().pose().load(matrix4f);
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

	protected void renderZeloBreadcrumbs(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
		List<Screen> history = ScreenOpener.getScreenHistory();
		if (history.isEmpty())
			return;

		history.add(0, minecraft.screen);
		int spacing = 20;

		List<String> names = history.stream()
			.map(NavigatableSimiScreen::screenTitle)
			.collect(Collectors.toList());

		int bWidth = names.stream()
			.mapToInt(s -> font.width(s) + spacing)
			.sum();

		MutableInt x = new MutableInt(width - bWidth);
		MutableInt y = new MutableInt(height - 18);
		MutableBoolean first = new MutableBoolean(true);

		if (x.getValue() < 25)
			x.setValue(25);

		ms.pushPose();
		ms.translate(0, 0, 600);
		names.forEach(s -> {
			int sWidth = font.width(s);
			UIRenderHelper.breadcrumbArrow(ms, x.getValue(), y.getValue(), 0, sWidth + spacing, 14, spacing / 2,
					new Color(0xdd101010), new Color(0x44101010));
			font.draw(ms, s, x.getValue() + 5, y.getValue() + 3, first.getValue() ? 0xffeeffee : 0xffddeeff);
			first.setFalse();

			x.add(sWidth + spacing);
		});
		ms.popPose();
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
