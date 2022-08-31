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

import net.createmod.catnip.gui.widget.BoxWidget;
import net.createmod.catnip.utility.animation.LerpedFloat;
import net.createmod.catnip.utility.lang.Lang;
import net.createmod.catnip.utility.theme.Color;
import net.createmod.catnip.utility.theme.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public abstract class NavigatableSimiScreen extends AbstractSimiScreen {

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
		if (!(screenHistory.get(0) instanceof NavigatableSimiScreen screen))
			return;

		addRenderableWidget(backTrack = new BoxWidget(31, height - 31 - 20)
				.withBounds(20, 20)
				.enableFade(0, 5)
				.withPadding(2, 2)
				.withCallback(() -> ScreenOpener.openPreviousScreen(this, null)));
		backTrack.fade(1);

		screen.initBackTrackIcon(backTrack);
	}

	/**
	 * Called when {@code this} represents the previous screen to
	 * initialize the {@code backTrack} icon of the current screen.
	 *
	 * @param backTrack The backTrack button of the current screen.
	 */
	protected abstract void initBackTrackIcon(BoxWidget backTrack);

	@Override
	public void render(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
		super.render(ms, mouseX, mouseY, partialTicks);
//		renderZeloBreadcrumbs(ms, mouseX, mouseY, partialTicks);
		if (backTrack == null)
			return;

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

	protected Component backTrackingComponent() {
		return Lang.builder("catnip")
				.translate("gui.step_back")
				.component();
	}

	@Override
	protected void renderWindowBackground(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
		if (backTrack != null) {
			int x = (int) Mth.lerp(arrowAnimation.getValue(partialTicks), -9, 21);
			int maxX = backTrack.x + backTrack.getWidth();

			if (x + 30 < backTrack.x)
				UIRenderHelper.breadcrumbArrow(ms, x + 30, height - 51, 0, maxX - (x + 30), 20, 5,
						Theme.Key.NAV_BACK_ARROW.p());

			UIRenderHelper.breadcrumbArrow(ms, x, height - 51, 0, 30, 20, 5, Theme.Key.NAV_BACK_ARROW.p());
			UIRenderHelper.breadcrumbArrow(ms, x - 30, height - 51, 0, 30, 20, 5, Theme.Key.NAV_BACK_ARROW.p());
		}

		if (transition.getChaseTarget() == 0 || transition.settled()) {
			renderBackground(ms);
			return;
		}

		renderBackground(ms);

		Screen lastScreen = ScreenOpener.getPreviouslyRenderedScreen();
		float transitionValue = transition.getValue(partialTicks);
		float scale = 1 + 0.5f * transitionValue;

		// draw last screen into buffer
		if (lastScreen != null && lastScreen != this && !transition.settled()) {
			ms.pushPose();
			UIRenderHelper.framebuffer.clear(Minecraft.ON_OSX);
			ms.translate(0, 0, -1000);
			UIRenderHelper.framebuffer.bindWrite(true);
			//TODO PonderTooltipHandler.enable = false;
			lastScreen.render(ms, mouseX, mouseY, partialTicks);
			//PonderTooltipHandler.enable = true;

			ms.popPose();
			ms.pushPose();

			// use the buffer texture
			minecraft.getMainRenderTarget()
				.bindWrite(true);

			Window window = minecraft.getWindow();
			int dpx = window.getGuiScaledWidth() / 2;
			int dpy = window.getGuiScaledHeight() / 2;
			if (lastScreen instanceof NavigatableSimiScreen navigableScreen) {
				dpx = navigableScreen.depthPointX;
				dpy = navigableScreen.depthPointY;
			}

			ms.translate(dpx, dpy, 0);
			ms.scale(scale, scale, 1);
			ms.translate(-dpx, -dpy, 0);
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			UIRenderHelper.drawFramebuffer(1f - Math.abs(transitionValue));
			RenderSystem.disableBlend();
			ms.popPose();
		}

		// modify current screen as well
		scale = transitionValue > 0 ? 1 - 0.5f * (1 - transitionValue) : 1 + .5f * (1 + transitionValue);
		ms.translate(depthPointX, depthPointY, 0);
		ms.scale(scale, scale, 1);
		ms.translate(-depthPointX, -depthPointY, 0);
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
